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

Run the **Release** workflow on `main`:

| Input | Default | |
|---|---|---|
| `version` | empty | Version suggested by the commits, or e.g. `1.0.0` |
| `dry_run` | off | Only show the plan and run the checks, nothing is created |

```bash
gh workflow run release.yml --ref main -f dry_run=true   # plan and checks
gh workflow run release.yml --ref main                   # release
```

Both do the same checks:

1. **Plan** in the summary of the run: changes since the last release, suggested version and warnings (commits that are not conventional, nothing visible to users). Fails if there are no changes or the version already exists.
2. **Build:** all tests, the jar, and its smoke test with ExifTool.

Without `dry_run` it continues:

3. **Publish:** release notes, `exifutils.jar` and its checksum. Publishing creates the tag `vX.Y.Z`.

Nothing is published if any check fails. Releases in this repository are immutable: the files and the tag cannot be changed afterwards and the version cannot be used again. Only the title and the notes can be edited. The repository owner can release, and only from `main`.

The plan can be printed locally as well:

```bash
python3 .github/scripts/release_plan.py
```
