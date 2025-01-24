# exifutils
A simple tool for organizing and renaming a bunch of media files chronologically by exif metadata.

Notes: 
What needs to be done:
Photos: nothing needed
Videos:
- [ ] Assume CreateDate is always in UTC
- [ ] If CreationDate is not present, it should be added with time zone, e.g. 2020-01-01T00:00:00+00:00
- [ ] Since CreateDate is in UTC and there is no time zone, we need to assume zone, pick this from settings file,
      or supplied via command line parameters
- [ ] Via ffmpeg there can be extracted location data, this could be used to retrieve time zone for correct conversion
- [ ] There may be situation where CreateDate is actually NOT in UTC, in this case we need to convert it to UTC
      and also write full date with offset to CreationDate tag