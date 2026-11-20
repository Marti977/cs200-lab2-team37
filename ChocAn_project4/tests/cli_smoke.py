"""Run the actual application across separate processes. Author: Faizan Khan."""
import datetime, os, pathlib, subprocess, tempfile
root = pathlib.Path(__file__).resolve().parents[1]
java = str(pathlib.Path(os.environ['JAVA_HOME']) / 'bin/java') if 'JAVA_HOME' in os.environ else 'java'
day = datetime.date.today().isoformat()
checks = 0
with tempfile.TemporaryDirectory(prefix='chocan-cli-') as folder:
    def run(lines):
        result = subprocess.run([java, '-cp', str(root/'build/classes'), 'chocan.Main'],
            input='\n'.join(lines)+'\n', text=True, capture_output=True, cwd=folder, timeout=30)
        assert result.returncode == 0, result.stderr
        assert 'Exception' not in result.stderr, result.stderr
        return result.stdout
    def expect(text, fragment):
        global checks
        assert fragment in text, (fragment, text)
        checks += 1
    first = run(['manager','wrong','operator','op123',
        '1','100000001','Alex Member','1 Main St','Tuscaloosa','AL','35401',
        '1','100000002','Sam Member','2 Main St','Tuscaloosa','AL','35401',
        '5','200000000','Dana Provider','3 Main St','Tuscaloosa','AL','35401','Counselor','demo@example.com',
        '10','100000002','suspended','0',
        'provider','pr123','2','3','999999999','3','100000002',
        '1','100000002',
        '1','100000001','not-a-date',
        '1','100000001',day,'999999',
        '1','100000001',day,'100001','N',
        '1','100000001',day,'100001','Y','First demo service','0',
        'manager','mg123',
        '2','','','3','','','100000001','4','','','200000000',
        '1','','','5','0','EXIT'])
    for fragment in ['Invalid username or password','Member added successfully','Provider added successfully',
        'CHOCAN PROVIDER DIRECTORY','Invalid member number','Member suspended','Invalid date',
        'Invalid service code','Service cancelled','Service recorded successfully',
        'CHOCAN MEMBER REPORT','CHOCAN PROVIDER REPORT','Overall fee: $75.00','Accounting complete']:
        expect(first, fragment)
    files = list(pathlib.Path(folder).glob('reports/*/summary.txt'))
    assert len(files) == 1
    expect(files[0].read_text(), 'Total consultations: 1')
    expect(next(pathlib.Path(folder).glob('reports/*/eft.csv')).read_text(), '75.00')
    # Second JVM process must load all saved data, then persist edits.
    second = run(['operator','op123','4','8',
        '2','100000001','Alex Updated','','','','','',
        '6','200000000','Dana Updated','','','','','','','0',
        'manager','mg123','2','','','0','EXIT'])
    for fragment in ['Alex Member','Sam Member','Dana Provider','Member updated successfully',
                     'Provider updated successfully','Overall fee: $75.00']:
        expect(second,fragment)
    # Third JVM verifies edits, deletions, and historical reporting after deletion.
    third = run(['operator','op123','4','8','3','100000001','Y','7','200000000','Y','0',
        'manager','mg123','1','','','3','','','100000001','4','','','200000000','0','EXIT'])
    for fragment in ['Alex Updated','Dana Updated','Member deleted successfully','Provider deleted successfully',
                     'Deleted member 100000001','Deleted provider 200000000','Overall fee: $75.00']:
        expect(third, fragment)
    # EOF at a menu must save and exit without an exception.
    fourth = run(['operator','op123'])
    expect(fourth, 'Input closed.')
    # Bad date ranges and unknown report targets must return to the manager menu.
    fifth = run(['manager','mg123','2','2026-11-20','2026-11-01',
                 '3','','','999999999','0','EXIT'])
    expect(fifth,'Report start date must be on or before end date')
    expect(fifth,'Member not found')
print(f'PASS: {checks} CLI assertions across five separate JVM processes.')
