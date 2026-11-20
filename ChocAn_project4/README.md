# ChocAn — Project 4, Team 37

**Updated Project 4 ZIP — use this one:** [ChocAn_Project4_Submission.zip](https://github.com/Marti977/cs200-lab2-team37/raw/refs/heads/main/ChocAn_project4/ChocAn_Project4_Submission.zip). Includes the completed Java application, final UML, run instructions, and demo guide. The other two ZIP files are original teammate uploads and are not the integrated version.

The integrated application is in **`src/chocan/`**. The original teammate ZIP uploads and loose Java files in this directory are preserved as source history; do not compile those copies together with `src/`.

## Compile and run

Requires **JDK 17 or later** (not just a JRE). No external Java dependencies, database, or IDE is required.

From `ChocAn_project4` on macOS/Linux:

```sh
sh run.sh
```

On Windows, run `run.bat`, or compile and run manually:

```text
mkdir build\classes
javac --release 17 -d build\classes src\chocan\*.java
java -cp build\classes chocan.Main
```

Manual macOS/Linux commands:

```sh
mkdir -p build/classes
javac --release 17 -d build/classes src/chocan/*.java
java -cp build/classes chocan.Main
```

For Eclipse: create a Java project using JDK 17+, copy the `src/chocan` package into its source folder, and run `chocan.Main`. The integrated project deliberately has no `module-info.java`. Keep a consistent working directory between runs so the same saved data file is used.

## Demo logins

| Role | Username | Password |
| --- | --- | --- |
| Operator | `operator` | `op123` |
| Provider | `provider` | `pr123` |
| Manager | `manager` | `mg123` |

These are fixed demonstration credentials. At the login prompt, enter `EXIT` to close the application. Enter `0` in a role menu to log out. EOF also exits cleanly.

The provider login is linked to provider number **`200000000`**. Add that provider from the operator menu before recording services. The program starts with empty member/provider data; it does not silently insert sample people.

## Required workflows

- **Operator:** add, edit, delete and list members/providers; record a service for a selected provider; manually update membership status.
- **Provider:** validate a member independently; validate and record a service; display/export the provider directory.
- **Manager:** run accounting; request summary, member or provider reports individually; display/export the provider directory.
- **Validation:** IDs have nine digits; service codes have six. Only active members and existing providers can receive billing records. Unknown codes, invalid/future dates and comments longer than 100 characters are rejected. Billing requires confirmation.
- **Reports:** enter an inclusive service-date range or press ENTER twice for the most recent seven calendar days, including today. Reports filter by **service date**, not the timestamp when the record was entered. Member reports are chronological; provider reports include consultations and fees; summaries aggregate by provider.
- **Accounting:** creates member reports, provider reports, a summary, and an EFT CSV containing each provider's total. It includes current accounts with no activity and historical accounts with activity in the period. Rerunning the same period refreshes files; it does not duplicate service records or execute payments.

## Files, persistence and assumptions

- Member/provider/service data: `data/member-provider-records.properties`.
- Report output: `reports/YYYY-MM-DD_to_YYYY-MM-DD/`.
- Provider directory: `reports/provider-directory.txt`, sorted alphabetically by service name.
- Changes save immediately. Exiting and restarting loads the saved records. Failed manager/service saves roll back the in-memory change. Corrupt data produces an error instead of being silently overwritten.
- Service records retain their original fee. Deleting a member/provider does not erase billed history. Reports label deleted accounts by their identifiers; their previous contact details are not retained. A deleted identifier with historical billing cannot be reused.
- The five service names/codes/fees are an explicitly labeled **demonstration catalog**, not an official ChocAn fee schedule. They are defined in `ServiceManager`.
- Acme membership updates are simulated manually. No Acme, email, bank or payment network is contacted. Accounting is run from the manager menu; there is no background scheduler. This avoids the original timer reading from the same console as an active user.
- The CLI is the implemented interface. No GUI extra credit is claimed. Persistence can be demonstrated by fully exiting and restarting.

## UML and demonstration

`ChocAn_Final_ClassDiagram.uxf` is the editable UMLet/UMLetino diagram of the 19 implemented application types. The PNG preview is also included. Constructors, ordinary getters/setters, private helpers, and library classes are omitted for readability; record fields and principal associations are shown. The earlier Project 3 diagram is not the final implementation diagram.

`DEMO_GUIDE.md` provides a reproducible demo sequence and a short explanation of class interactions. `GOOGLE_DOC_TEXT.txt` contains Faizan's contribution section and the system explanation for the shared document.

## Verification

```sh
sh test.sh
python3 tests/cli_smoke.py
```

The Java checks need only JDK 17+. The optional CLI process test also needs Python 3. The tests use temporary data, not your demonstration records. The Java suite checks validation, persistence, date filtering, exact totals, repeated accounting, save rollback, CRUD, and historical records. The process test exercises login/menu workflows and persistence across separate JVM runs.

No separate written testing report is required by Project 4; these scripts are provided to make verification repeatable.

## Contribution provenance

- Alyssa Duncan: original `Member`, `Provider`, `MemberManager`, and `ProviderManager` implementations.
- Maggie Skinner: uploaded `DataStore`, `Role`, `User`, and `ServiceRecord` implementations.
- Original `Login`, `Main`, `Operator`, and `ProviderUser` came from the `taking10` repository upload; this attribution is preserved without guessing the uploader's full name.
- Faizan Khan: service manager/catalog, provider directory, reports, accounting, manager menu, shared-storage integration, input validation, persistent CRUD/rollback, safe console execution, final UML, build/run instructions, and integration checks.

Use the repository history and each teammate's confirmation when describing the rest of the team's contributions in the video. Do not assign hours or percentages without their input.
