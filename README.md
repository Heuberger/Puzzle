# Puzzle

![splash](src/pics/splash.png)

A Jigsaw Puzzle 
by Carlos F. Heuberger

*Use at your own risk - no warranties!* For my family and myself.

Copyright: Carlos F. Heuberger. All rights reserved.



Please note that **no** User-Generated Content in this repository is licensed!   You can execute the following files from the `Puzzle/dist/` folder, to play the game:

- `puzzle.jar` (using a Java Virtual Machine); or
- `*.bat` files (to start the game).

Other then that, you are not allowed to use, change and/or redistribute any User-Generated Content, except as described in Github Terms of Service (Section D.5). Particularly, you are not allowed to use or distribute any source code or any resource posted in this repository.



## Instructions

### Version

- update `Implementation-Version` in `src/manifest.txt`
- update `version` from `jnlp` tag in `src/puzzle.jnlp`
- create `JAR`
- sign `JAR` using `. src/signjar.sh`

### Java Web Start

- copy `src/puzzle.jnlp` to `docs/`
- copy `dist/puzzle.jar` to `docs/`

### ZIP

- copy `dist/` folder to new folder `Puzzle-<version>`
- zip `Puzzle-<version>` folder to `Puzzle-<version>.zip`
- adjust `ZIP` link in `docs/index.md` - two times Version (tag and file)
- `commmit` and `tag -s v<version> -m v<version>`
- `push`, create release for that tag and attach the `ZIP`

### Pictures

Originals are available under `src/pics/` as Gimp files.

- `icon.png`
  scaled to 32x32
  copy to `docs/` and `src/java/cfh/puzzle/resources`
- `splash.png`
  scaled to 640x320
  copy to `docs/` and `src/java/cfh/puzzle/resources`
- `social.png`
  used as Social Preview on GitHub



