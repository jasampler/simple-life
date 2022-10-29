# SimpleLife
Simple command-line version of Conway's Game of Life

    $ echo "oooooooo.ooooo...ooo......ooooooo.ooo" | java -jar SimpleLife.jar --sepline -g 550 -m -11,0 | more

    $ java -jar SimpleLife.jar -h
    Usage: java SimpleLife [OPTION]... < INPUT_FILE
    Simple command-line version of Conway's Game of Life (B3/S23).

    Gets the cells of the board reading lines from the standard input
    interpreting by default the symbols o and . as ALIVE/DEAD cells,
    and then prints the ASCII board in each generation of cells.
    The position (0, 0) is assigned to the first ALIVE cell found, so
    the board read will be the same if the cells are just shifted:
     . . . . . . . 
     . . . o o . . 
     . . . . o o . 
     . . . . o . . 

      -g|--gen NUM_GEN
                  Number of generations to calculate, 0 by default!
      -p|--print [NUM_PRINT]
                  Number of boards to print counting from the last,
                  by default is NUM_GEN+1 to print the generation 0
      -s|--size NUM_ROWS,NUM_COLS
                  The size of the portion of the board to be printed,
                  as the number of rows and columns, by default 23,39
      -m|--min [MIN_ROW,MIN_COL]
                  The minimum row and minimum colum to be printed, or
                  the coordinates of the first upper-left cell shown,
                  by default calculated to center the board in (0, 0)
      --startline [LINE] / --endline [LINE] / --sepline [LINE]
                  Prints the given line before each board, after it
                  or between two boards. By default only --sepline QQ
                  is active, but can be removed just using --sepline
      -o|--outfmt ",ALIVE,DEAD[,SEP,START,END]"
                  Format to print the lines, using the first character
                  to separate the fields that will be concatenated as:
                  START + (ALIVE|DEAD) + [SEP + (ALIVE|DEAD)]... + END
                  The default format is ",o,., , ," but try these:
                    ",[],  " / ",@,_,|,|,|" / ",[_], _ " / ",(o), . "
      -i|--infmt ,ALIVE,DEAD
                  The two character sequences to recognize from the
                  input for ALIVE and DEAD cells, ",o,." by default
      -h|--help   Prints this help

    Example of use with the default command-line options:

      java SimpleLife --gen 0 --print 1 --size 23,39 --min -11,-19 \
                --sepline "" --outfmt ",o,., , ," --infmt ",o,."
