# exifutils
A simple tool for organizing and renaming a bunch of media files chronologically by exif metadata.

Notes: 
What needs to be done:
Photos: nothing needed
Videos:

- [ ] If CreationDate is not present, it should be added with time zone, e.g. 2020-01-01T00:00:00+00:00,
      add WRITE flag -w to command line to allow write of that time zone data after analysis
- if that creationdate with zone is present, no need to analyse the gps - i think this is already implemented
- add --shift flag to set-date command that allows shifting the date by a certain amount of time and to fix time zone data 
 because some files may be incorrectly tagged with the wrong time zone, its probably rate tough 

Onplus newest does not add location tag to video

## Command Line Arguments Processing

For each command line argument:
    - If the argument is a path to a directory:
        - Add all files from the directory, recursively
    - Else:
        - Add the file individually and continue