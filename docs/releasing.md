# Snapshots and Releases

## Snapshots

Every push to `main` builds and tests the application and stores the runnable jar as the artifact `exifutils-jar` of the **Java Build** workflow (90 days). Pull requests do the same (7 days). GitHub requires a login to download artifacts.

```bash
# newest successful build of main
run=$(gh run list -R kubisoftsk/exifutils -w build.yml -b main -e push -s success -L 1 --json databaseId -q '.[0].databaseId')
gh run download "$run" -R kubisoftsk/exifutils -n exifutils-jar -D exifutils-snapshot
cat exifutils-snapshot/build-info.txt
java -jar exifutils-snapshot/exifutils.jar
```

Native binaries are not part of snapshots and releases yet, see [#53](https://github.com/kubisoftsk/exifutils/issues/53).

## Versions

The project uses [Semantic Versioning](https://semver.org/). The released version is in the git tag (`vX.Y.Z`); the version in `pom.xml` (property `revision`) is only the default for local and snapshot builds. Nothing is committed to `main` during a release.

The next version is suggested from [conventional commits](https://www.conventionalcommits.org/) since the last release:

| Commits contain | Before 1.0.0 | From 1.0.0 |
|---|---|---|
| Breaking change: `feat!:`, `fix!:` or a `BREAKING CHANGE:` footer | minor | major |
| `feat` | minor | minor |
| Anything else | patch | patch |

Version 1.0.0 is released by entering it by hand.

## Release

1. **Plan:** run the **Release** workflow on `main` with default inputs. The summary of the run shows the changes since the last release, the suggested version and warnings (commits that are not conventional, nothing visible to users). Nothing is created.
2. **Draft:** run it again with *draft* checked, optionally with another version. It runs all tests, builds the jar, runs a smoke test of it with ExifTool and creates a draft release with a checksum.
3. **Publish:** check the draft on the Releases page, edit the notes if needed and publish it. Publishing creates the tag. Releases in this repository are immutable, files cannot be changed afterwards.

To start over, delete the draft and run the workflow again.

The plan can be printed locally as well:

```bash
python3 .github/scripts/release_plan.py
```
