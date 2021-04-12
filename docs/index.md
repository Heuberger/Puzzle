# Jigaw Puzzle Game

![splash](splash.png)

*use at your own risk - no warranties!*

## Requirements

Java 8 (`JRE8`) must be installed. Version `8u202` is recommended, license of newer versions have changed - please check! It can be found at [Java Archive](https://www.oracle.com/java/technologies/javase/javase8-archive-downloads.html).

## Java Web Start

Download and execute: [puzzle.jnlp](puzzle.jnlp).

## How to Play

Open an image file to create a new puzzle
or
open a previously saved puzzle to continue playing.

*Note:* puzzles are **not saved** automatically!

#### Moving Pieces

* Use the `LEFT` button to drag and drop pieces. 
  If a piece is dropped near the correct neighbour(s), the pieces will snap together.
* Hold the `CTRL` Key and click on a connected piece to disconnect it.
* Use the scroll wheel or double-click (`LEFT` or `RIGHT` button) to rotate the piece(s) under the cursor.

#### General

* `RIGHT` click background to open menu.
* Use `LEFT` button on background to drag the whole surface.
* `CTRL`-digit or `CTRL`-letter to save current surface position under that key. 
  The corresponding digit or letter, without `CTRL`, to move to the corresponding saved position.
* `BACKSPACE` to move back to last position.

#### Marking

Marked (selected) pieces can be rearranged by selecting the `Arrange` menu point. If no pieces are marked, all pieces will be rearranged. Connected pieces are never rearranged. The pieces will be arranged horizontally starting at the current location, or vertically if the `CTRL` key is pressed when the menu is selected.

* Pieces can be marked (selected) and unmarked by `SHIFT-LEFT`-clicking on them.
* Use `SHIFT-SPACE` to unmark all pieces.

#### Selection Group

Pieces can be grouped together in Selection Groups named by a digit or letter. Activating such group will mark the pieces of that group. While the group is active, marking/unmarking a piece will add/remove that piece from that group.

- `SHIFT`-digit or `SHIFT`-letter to activate Selection Group.
- `SHIFT`-`SPACE` to deactivate any group and unmark all pieces.
- `CTRL`-`SHIFT`-digit or `CTRL`-`SHIFT`-letter to save the actually marked pieces to a Selection Group.

#### Background

Selecting the `Background` menu option, a background image can be loaded. A color can be chosen, by entering `color` as file name.