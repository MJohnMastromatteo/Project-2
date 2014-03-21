import java.util.Scanner;

public class Game {

    //
    // Public
    //

    // Globals
    public static final boolean DEBUGGING  = false; // Debugging flag.
    public static final int MAX_LOCALES = 8;    // Total number of rooms/locations we have in the game.
    public static int currentLocale = 0;        // Player starts in locale 0.
    public static String command;               // What the player types as he or she plays the game.
    public static boolean stillPlaying = true; // Controls the game loop.
    public static Locale[] locations;           // An uninitialized array of type Locale. See init() for initialization.
    public static int[][]  nav;                 // An uninitialized array of type int int.
    public static int moves = 0;                // Counter of the player's moves.
    public static int score = 0;                // Tracker of the player's score.
    public static int hasVisited[] = {0,0,0,0,0,0,0,0,0}; // Sets when a player has visited a new place
    public static double achievementRatio = 0.0; // Ratio for moves/score.
    public static int[] playerInventory = {0,0,0,0,0}; // Keeps track of which items a player has
    public static boolean hasMap = false; //Checks if the player has the map
    public static boolean inShop = false; // Checks if the player is in the shop

    public static void main(String[] args) {
        if (DEBUGGING) {
            // Display the command line args.
            System.out.println("Starting with args:");
            for (int i = 0; i < args.length; i++) {
                System.out.println(i + ":" + args[i]);
            }
        }

        // Set starting locale, if it was provided as a command line parameter.
        if (args.length > 0) {
            try {
                int startLocation = Integer.parseInt(args[0]);
                // Check that the passed-in value for startLocation is within the range of actual locations.
                if ( startLocation >= 0 && startLocation <= MAX_LOCALES) {
                    currentLocale = startLocation;
                } else {
                    System.out.println("WARNING: passed-in starting location (" + args[0] + ") is out of range.");
                }
            } catch(NumberFormatException ex) {
                System.out.println("WARNING: Invalid command line arg: " + args[0]);
                if (DEBUGGING) {
                    System.out.println(ex.toString());
                }
            }
        }

        // Get the game started.
        startGame();
        init();
        updateDisplay();

        // Game Loop
        while (stillPlaying) {
            getCommand();
            navigate();
            updateDisplay();
        }

        // We're done. Thank the player and exit.
        System.out.println("Thank you for playing.");
    }

    //
    // Private
    //

    private static void startGame(){
    	System.out.println("Welcome to Land of Aldaria.");
    	System.out.println("You begin your journey on a long road to the main city, Terzin.");
    	System.out.println("As you journey down the path you feel the breeze across your face.");
    }
    
    private static void init() {
        // Initialize any uninitialized globals.
        command = new String();
        stillPlaying = true;   // TODO: Do we need this?
        
        // Set up the location instances of the Locale class.
        Locale loc0 = new Locale(0);
        loc0.setName("Path to Terzin");
        loc0.setDesc("You are on a long road to the main city, Terzin.");
        loc0.setDirections("North, East, South.");
        loc0.setWater("No");
        loc0.setItem("a Map");
        
        Locale loc1 = new Locale(1);
        loc1.setName("Plains");
        loc1.setDesc("A field of endless tall grass.");
        loc1.setDirections("North, East.");
        loc1.setWater("No");
        loc1.setItem("nothing");
        
        Locale loc2 = new Locale(2);
        loc2.setName("Woods");
        loc2.setDesc("It seems like I'm walking in circles through the neverending mass of tree's around me.");
        loc2.setDirections("East, South.");
        loc2.setWater("No");
        loc2.setItem("nothing");
        
        Locale loc3 = new Locale(3);
        loc3.setName("Market");
        loc3.setDesc("A crowd surrounds you as you hear cheers all around. You see shops lined up with goods of all kinds.");
        loc3.setDirections("West, East, South.");
        loc3.setWater("No");
        loc3.setItem("nothing");
        
        Locale loc4 = new Locale(4);
        loc4.setName("Town Square");
        loc4.setDesc("People and wagons bustle around you as the guards create a path for the long line you stand on.");
        loc4.setDirections("North, West, East, South.");
        loc4.setWater("No");
        loc4.setItem("nothing");
        
        Locale loc5 = new Locale(5);
        loc5.setName("River");
        loc5.setDesc("You see a stream of water that passes next to you, your reflection glimmaring in the small waves.");
        loc5.setDirections("North, East, West.");
        loc5.setWater("Yes");
        loc5.setItem("nothing");
        
        Locale loc6 = new Locale(6);
        loc6.setName("Magick Shoppe");
        loc6.setDesc("Welcome to the Magick Shoppe, let me show you my wares.");
        loc6.setDirections("West, South.");
        loc6.setWater("No");
        loc6.setItem("nothing");
        
        Locale loc7 = new Locale(7);
        loc7.setName("Palace");
        loc7.setDesc("You enter the palace grounds and guards immediately escort you out because you're not royalty...");
        loc7.setDirections("South, North, West.");
        loc7.setWater("No");
        loc7.setItem("nothing");
        
        Locale loc8 = new Locale(8);
        loc8.setName("Port");
        loc8.setDesc("You walk in and smell a cool breeze of the saltwater and fish. Ships surround the docks with sailor talk all around you.");
        loc8.setDirections("North, West.");
        loc8.setWater("Yes");
        loc8.setItem("nothing");
        
        // Set up the location array.
        locations = new Locale[9];
        locations[2] = loc2; // "Woods";      //  ^
        locations[0] = loc0; // "Long Path";  //  N
        locations[1] = loc1; // "Plains";     //  |
        locations[3] = loc3; // Market
        locations[4] = loc4; // Town Square
        locations[5] = loc5; // River
        locations[6] = loc6; // Magick Shoppe
        locations[7] = loc7; // Palace
        locations[8] = loc8; // Port

        if (DEBUGGING) {
            System.out.println("All game locations:");
            for (int i = 0; i < locations.length; ++i) {
                System.out.println(i + ":" + locations[i].toString());
            }
        }
        // Set up the navigation matrix.
        nav = new int[][] {
                                 /* N   S   E   W */
                                 /* 0   1   2   3 */
         /* nav[0] for loc 0 */  {  2,  1,  4, -1 },
         /* nav[1] for loc 1 */  {  0, -1,  5, -1 },
         /* nav[2] for loc 2 */  { -1,  0,  3, -1 },
         /* nav[3] for loc 3 */  { -1,  4,  6,  2 },
         /* nav[4] for loc 4 */  {  3,  5,  7,  0 },
         /* nav[5] for loc 5 */  {  4, -1,  8,  1 },
         /* nav[6] for loc 6 */  { -1,  7, -1,  3 },
         /* nav[7] for loc 7 */  {  6,  8, -1,  4 },
         /* nav[8] for loc 8 */  {  7, -1, -1,  5 }
        };

        createMagicItems();
    }

    private static void updateDisplay() {
        System.out.println(locations[currentLocale].getText());
    }

    private static void getCommand() {
        System.out.print("[" + moves + " moves, score " + score +  " ratio + "+ achievementRatio + "] ");
        Scanner inputReader = new Scanner(System.in);
        command = inputReader.nextLine();  // command is global.
    }

    private static void navigate() {
        final int INVALID = -1;
        int dir = INVALID;  // This will get set to a value > 0 if a direction command was entered.

        if (        command.equalsIgnoreCase("north") || command.equalsIgnoreCase("n") ) {
            dir = 0;
        } else if ( command.equalsIgnoreCase("south") || command.equalsIgnoreCase("s") ) {
            dir = 1;
        } else if ( command.equalsIgnoreCase("east")  || command.equalsIgnoreCase("e") ) {
            dir = 2;
        } else if ( command.equalsIgnoreCase("west")  || command.equalsIgnoreCase("w") ) {
            dir = 3;
        } else if ( command.equalsIgnoreCase("quit")  || command.equalsIgnoreCase("q")) {
            quit();
        } else if ( command.equalsIgnoreCase("help")  || command.equalsIgnoreCase("h")) {
            help();
        } else if ( command.equalsIgnoreCase("map")  || command.equalsIgnoreCase("m")) {
            openMap();
        } else if ( command.equalsIgnoreCase("dance")  || command.equalsIgnoreCase("d")) {
            dance();  
        } else if ( command.equalsIgnoreCase("inventory")  || command.equalsIgnoreCase("i")) {
            managePlayerInventory(); 
        } else if ( command.equalsIgnoreCase("shop")  || command.equalsIgnoreCase("sh")) {
            enterMagickShoppe();
        } else if (command.equalsIgnoreCase("pmap") ) {
        	enterMagickShoppe();
        } else if (command.equalsIgnoreCase("pring")) {
        	enterMagickShoppe();
        } else if (command.equalsIgnoreCase("pclaymore")) {
        	enterMagickShoppe();
        } else if (command.equalsIgnoreCase("pcloak")) {
        	enterMagickShoppe();
        } else if (command.equalsIgnoreCase("pshield")) {
        	enterMagickShoppe();
        } else if (command.equalsIgnoreCase("leave")) {
        	enterMagickShoppe();
        } else {
        	System.out.println("That is not a valid command.");
        };

        if (dir > -1) {   // This means a dir was set.
            int newLocation = nav[currentLocale][dir];
            //All the locations and set it to one when a location is newly visited
            if (newLocation == INVALID) {
                System.out.println("You cannot go that way.");
            } else {
                if(newLocation == 0) {
                	while(hasVisited[0] == 0) {
                		score += 5;
                		System.out.println("This is your first time here!");
                		hasVisited[0] = 1;
                	}
                } else if(newLocation == 1) {
                	while(hasVisited[1] == 0) {
                		score += 5;
                		System.out.println("This is your first time here!");
                		hasVisited[1] = 1;
                	}
                } else if(newLocation == 2) {
                	while(hasVisited[2] == 0) {
                		score += 5;
                		System.out.println("This is your first time here!");
                		hasVisited[2] = 1;
                	}
                	
                } else if(newLocation == 3) {
                	while(hasVisited[3] == 0) {
                		score += 5;
                		System.out.println("This is your first time here!");
                		hasVisited[3] = 1;
                	}
 
                } else if(newLocation == 4) {
                	while(hasVisited[4] == 0) {
                		score += 5;
                		System.out.println("This is your first time here!");
                		hasVisited[4] = 1;
                	}
                	
                } else if(newLocation == 5) {
                	while(hasVisited[5] == 0) {
                		score += 5;
                		System.out.println("This is your first time here!");
                		hasVisited[5] = 1;
                	}
                	
                } else if(newLocation == 6) {
                	while(hasVisited[6] == 0) {
                		score += 5;
                		System.out.println("This is your first time here!");
                		hasVisited[6] = 1;
                	}
                	
                } else if(newLocation == 7) {
                	while(hasVisited[7] == 0) {
                		score += 5;
                		System.out.println("This is your first time here!");
                		hasVisited[7] = 1;
                	}
                	
                } else if(newLocation == 8) {
                	while(hasVisited[8] == 0) {
                		score += 5;
                		System.out.println("This is your first time here!");
                		hasVisited[8] = 1;
                	}
                	
                } 
                currentLocale = newLocation;
                moves = moves + 1;
                achievementRatio = score/moves;
            }
        }
    }

    private static void help() {
        System.out.println("The commands are as follows:");
        System.out.println("   n/north");
        System.out.println("   s/south");
        System.out.println("   e/east");
        System.out.println("   w/west");
        System.out.println("   q/quit");
        System.out.println("   m/map");
        System.out.println("   d/dance");
        System.out.println("   sh/shop");
        System.out.println("Shop Commands:");
        System.out.println("     pmap");
        System.out.println("     pring");
        System.out.println("     pclaymore");
        System.out.println("     pcloak");
        System.out.println("     pshield");
        System.out.println("     leave");
    }
    
    private static void quit() {
        stillPlaying = false;
    }
    
    private static void dance(){
    	System.out.println("You dance around like a mad man.");
    }
    
    private static void openMap() {
	    	if(hasMap == true) {
		        if(currentLocale == 0) {
			    	System.out.println("Key: C = Current Location ^ = Woods, $ = Market, ? = Magick Shoppe, = = Path, ");
			    	System.out.println("% = Town Square, * = Palace, ~ = Plains, &&& = River, @@@ = Port");
		        	System.out.println("+--------------+");
		        	System.out.println("| ^^^ $$$|  ???|");
		        	System.out.println("|S=C==%%%| *** |");
		        	System.out.println("| ~~~ &&&| @@@ |");
		        	System.out.println("+--------------+");
		        } else if(currentLocale == 1) {
			    	System.out.println("Key: C = Current Location ^ = Woods, $ = Market, ? = Magick Shoppe, = = Path, ");
			    	System.out.println("% = Town Square, * = Palace, ~ = Plains, &&& = River, @@@ = Port");
		        	System.out.println("+--------------+");
		        	System.out.println("| ^^^ $$$|  ???|");
		        	System.out.println("|S====%%%| *** |");
		        	System.out.println("| ~C~ &&&| @@@ |");
		        	System.out.println("+--------------+");
		        } else if(currentLocale == 2) {
			    	System.out.println("Key: C = Current Location ^ = Woods, $ = Market, ? = Magick Shoppe, = = Path, ");
			    	System.out.println("% = Town Square, * = Palace, ~ = Plains, &&& = River, @@@ = Port");
		        	System.out.println("+--------------+");
		        	System.out.println("| ^C^ $$$|  ???|");
		        	System.out.println("|S====%%%| *** |");
		        	System.out.println("| ~~~ &&&| @@@ |");
		        	System.out.println("+--------------+");
		        } else if(currentLocale == 3) {
			    	System.out.println("Key: C = Current Location ^ = Woods, $ = Market, ? = Magick Shoppe, = = Path, ");
			    	System.out.println("% = Town Square, * = Palace, ~ = Plains, &&& = River, @@@ = Port");
		        	System.out.println("+--------------+");
		        	System.out.println("| ^^^ $C$|  ???|");
		        	System.out.println("|S====%%%| *** |");
		        	System.out.println("| ~~~ &&&| @@@ |");
		        	System.out.println("+--------------+");
		        } else if(currentLocale == 4) {
			    	System.out.println("Key: C = Current Location ^ = Woods, $ = Market, ? = Magick Shoppe, = = Path, ");
			    	System.out.println("% = Town Square, * = Palace, ~ = Plains, &&& = River, @@@ = Port");
		        	System.out.println("+--------------+");
		        	System.out.println("| ^^^ $$$|  ???|");
		        	System.out.println("|S====%C%| *** |");
		        	System.out.println("| ~~~ &&&| @@@ |");
		        	System.out.println("+--------------+");
		        } else if(currentLocale == 5) {
			    	System.out.println("Key: C = Current Location ^ = Woods, $ = Market, ? = Magick Shoppe, = = Path, ");
			    	System.out.println("% = Town Square, * = Palace, ~ = Plains, &&& = River, @@@ = Port");
		        	System.out.println("+--------------+");
		        	System.out.println("| ^^^ $$$|  ???|");
		        	System.out.println("|S====%%%| *** |");
		        	System.out.println("| ~~~ &C&| @@@ |");
		        	System.out.println("+--------------+");
		        } else if(currentLocale == 6) {
			    	System.out.println("Key: C = Current Location ^ = Woods, $ = Market, ? = Magick Shoppe, = = Path, ");
			    	System.out.println("% = Town Square, * = Palace, ~ = Plains, &&& = River, @@@ = Port");
		        	System.out.println("+--------------+");
		        	System.out.println("| ^^^ $$$|  ?C?|");
		        	System.out.println("|S====%%%| *** |");
		        	System.out.println("| ~~~ &&&| @@@ |");
		        	System.out.println("+--------------+");
		        } else if(currentLocale == 7) {
			    	System.out.println("Key: C = Current Location ^ = Woods, $ = Market, ? = Magick Shoppe, = = Path, ");
			    	System.out.println("% = Town Square, * = Palace, ~ = Plains, &&& = River, @@@ = Port");
		        	System.out.println("+--------------+");
		        	System.out.println("| ^^^ $$$|  ???|");
		        	System.out.println("|S====%%%| *C* |");
		        	System.out.println("| ~~~ &&&| @@@ |");
		        	System.out.println("+--------------+");
		        } else if(currentLocale == 8) {
			    	System.out.println("Key: C = Current Location ^ = Woods, $ = Market, ? = Magick Shoppe, = = Path, ");
			    	System.out.println("% = Town Square, * = Palace, ~ = Plains, &&& = River, @@@ = Port");
		        	System.out.println("+--------------+");
		        	System.out.println("| ^^^ $$$|  ???|");
		        	System.out.println("|S====%%%| *** |");
		        	System.out.println("| ~~~ &&&| @C@ |");
		        	System.out.println("+--------------+");
		        } 
	    	}
    	}
    
    private static void enterMagickShoppe() {
    	if(currentLocale == 6) {
    		inShop = true;
    		if(inShop == true) {
	    		System.out.println("Here's what I have.");
	        	if(playerInventory[0] == 0) {
	        		System.out.println("Map.");
	        	}
	        	if(playerInventory[1] == 0) {
	        		System.out.println("Ruby Ring");
	        	}
	        	if(playerInventory[2] == 0) {
	        		System.out.println("Claymore");
	        	}
	        	if(playerInventory[3] == 0) {
	        		System.out.println("Cloak of Darkness");
	        	}
	        	if(playerInventory[4] == 0) {
	        		System.out.println("Shield of Divine");
	        	}
	        	System.out.println("Want to purchase something?");
	            if (command.equalsIgnoreCase("pmap") ) {
	            	playerInventory[0] = 1;
	            	hasMap = true;
	            } else if (command.equalsIgnoreCase("pring")) {
	            	playerInventory[1] = 1;
	            } else if (command.equalsIgnoreCase("pclaymore")) {
	            	playerInventory[2] = 1;
	            } else if (command.equalsIgnoreCase("pcloak")) {
	            	playerInventory[3] = 1;
	            } else if (command.equalsIgnoreCase("pshield")) {
	            	playerInventory[4] = 1;
	            } else if (command.equalsIgnoreCase("leave")) {
	            	inShop = false;
	            }
    		}
    	} else {
    		System.out.println("You are not near the shop.");
    	}
    }
    
    private static void managePlayerInventory() {
    	System.out.println("Inventory:");
    	System.out.println("-------------------");
    	if(playerInventory[0] == 1) {
    		System.out.println("Map.");
    	}
    	if(playerInventory[1] == 1) {
    		System.out.println("Ruby Ring");
    	}
    	if(playerInventory[2] == 1) {
    		System.out.println("Claymore");
    	}
    	if(playerInventory[3] == 1) {
    		System.out.println("Cloak of Darkness");
    	}
    	if(playerInventory[4] == 1) {
    		System.out.println("Shield of Divine");
    	}
    	System.out.println("-------------------");
    }

    private static void createMagicItems() {
        // Create the list manager for our magic items.
        List0 magicItems  = new List0();
        magicItems.setName("Magic Items");
        magicItems.setDesc("These are the magic items.");
        magicItems.setHead(null);

        // Create some magic items and put them in the list.
        ListItem i1 = new ListItem();
        i1.setName("Ruby ring");
        i1.setDesc("A bright gold ring with a ruby in the middle.");
        i1.setCost(5.0);

        ListItem i2 = new ListItem();
        i2.setName("Claymore");
        i2.setDesc("A double edged sword that looks like it's been used before.");
        i2.setCost(10.0);
        
        ListItem i3 = new ListItem();
        i3.setName("Cloak of Darkness");
        i3.setDesc("A cloak with the same color as the night sky.");
        i3.setCost(10.0);
        
        ListItem i4 = new ListItem();
        i4.setName("Shield of the Divine");
        i4.setDesc("A massive bright colored shield, looks like it can block any blow.");
        i4.setCost(100.0);
        
        // Link it all up.
        magicItems.setHead(i1);
        i1.setNext(i2);
        i2.setNext(i3);
        i3.setNext(i4);
        i4.setNext(null);

        //System.out.println(magicItems.toString());
    }

}
