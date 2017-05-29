import java.net.Socket;
import java.io.*;
import java.util.Arrays;
import java.util.Scanner;

public class TicTacToeClient{

	public static void main(String[] args)throws Exception{
		try(Socket socket = new Socket("codebank.xyz", 38006)){
			
			InputStream fromServer = socket.getInputStream();
			OutputStream toServer = socket.getOutputStream();
			ObjectOutputStream objectToServer= new ObjectOutputStream(toServer);
			ObjectInputStream objectFromServer = new ObjectInputStream(fromServer);
			//Establish connection
			establishConnection(objectToServer);
			//Start game
			Message message = startNewGame(objectToServer, objectFromServer);
			BoardMessage board = null;
			while(true){
				//if message is board message
				if(message.getType().equals(MessageType.BOARD)){
					board = (BoardMessage)message;
					//If game is not over, ask player for move and send the move message to server
					if(board.getStatus() == BoardMessage.Status.IN_PROGRESS){
						System.out.println("Turn number: "+board.getTurn());
						printBoard(board.getBoard());
						MoveMessage move = makeMove();
						objectToServer.writeObject(move);
					}
					//If there is error, print out "error"
					else if(board.getStatus() == BoardMessage.Status.ERROR){
						System.out.println("error");
					}
					//When game is not in progress and no error, the game is ended, so break the while loop
					else{
						break;
					}
					
				}
				// if message is a error message, print the error message and get a new move to send to server
				else if(message.getType().equals(MessageType.ERROR)){
					ErrorMessage error = (ErrorMessage)message;
					System.out.println(error.getError());
					printBoard(board.getBoard());
					MoveMessage move = makeMove();
					objectToServer.writeObject(move);
				}
				message = (Message)objectFromServer.readObject();
			}

			//after ending the game, print the board and determine who win the game or its a stalemate
			printBoard(board.getBoard());
			if(board.getStatus() == BoardMessage.Status.PLAYER1_VICTORY){
				System.out.println("You win!");
			}
			else if(board.getStatus() == BoardMessage.Status.PLAYER2_VICTORY){
				System.out.println("You lose!");
			}
			else if(board.getStatus() == BoardMessage.Status.PLAYER1_SURRENDER){
				System.out.println("You surrendered");
			}
			else if(board.getStatus() == BoardMessage.Status.PLAYER2_SURRENDER){
				System.out.println("Server surrendered");
			}
			else if(board.getStatus() == BoardMessage.Status.STALEMATE){
				System.out.println("Tie Game");
			}

			
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}

	//establish the connection
	public static void establishConnection(ObjectOutputStream oos)throws Exception{
		String name = getName();
		ConnectMessage connection = new ConnectMessage(name);
		oos.writeObject(connection);
	}

	//get the user name
	public static String getName(){
		Scanner keyboard = new Scanner(System.in);
		System.out.println("Please enter you name ");
		String name = keyboard.nextLine();
		return name;
	}

	//start a new game
	public static Message startNewGame(ObjectOutputStream oos, ObjectInputStream ois)throws Exception{
		System.out.println("Start a new game!\n");
		oos.writeObject(new CommandMessage(CommandMessage.Command.NEW_GAME));
		return (Message)ois.readObject();
	}

	//Get the 2D array board and print it as the following
	//  00 | 01 | 02 
	// --------------
	//  10 | 11 | 12 
	// --------------
	//  20 | 21 | 22 
	public static void printBoard(byte[][] board){
		for(int i = 0; i < board.length; i++){
			for(int j = 0; j < board.length; j++){
				if(board[i][j] == 0){
					System.out.print(" "+i+j+" ");
				}
				else if(board[i][j] == 1){
					System.out.print(" X ");
				}
				else{
					System.out.print(" O ");
				}
				if((j+1)%3 == 0 && i != 2){
					System.out.print("\n--------------\n");
				}
				else if(j != 2){
					System.out.print("|");
				}
			}
		}
		System.out.print("\n");
	}

	//Ask for a move, and return a move message 
	public static MoveMessage makeMove(){
		
		byte row = -1;
		byte col = -1;
		do{
			System.out.println("Select a move by entering the row number (first digit).");
			Scanner keyboard = new Scanner(System.in);
			if(!keyboard.hasNextInt()){
				continue;
			}
			else{
				row = keyboard.nextByte();
			}
			
		}while(validateMove(row)==false);
		
		do{
			System.out.println("Enter the colume number (second digit).");
			Scanner keyboard = new Scanner(System.in);
			if(!keyboard.hasNextByte()){

				keyboard.reset();
				continue;
			}
			else{
				col = keyboard.nextByte();
			}
		}while(validateMove(col)==false);

		MoveMessage move = new MoveMessage(row, col);
		return move;
	}

	//validate the move input to be only from 0 to 2
	public static boolean validateMove(byte move){
		if(move >= 0 && move <=2){
			return true;
		}
		else return false;
	}
}