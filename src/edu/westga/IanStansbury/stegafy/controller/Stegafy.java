/**
 * 
 */
package edu.westga.IanStansbury.stegafy.controller;

import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Scanner;

import javax.imageio.ImageIO;

/**
 * @author Ian
 * 
 */
public class Stegafy {

	static int messageImageHeight;
	static int messageImageWidth;
	static int inputImageHeight;
	static int inputImageWidth;
	static final String BINARYSTOP = "0010001101000101010011110100110100100011"; 

	/**
	 * @param args
	 * @throws FileNotFoundException
	 */
	public static void main(String[] args) throws FileNotFoundException {

		for (String s : args) {
			System.out.println(s);
		}

		// check for correct number of command line arguments
		if (!(args.length > 1) && !(args.length < 4)) {
			System.out
					.println("The following command-line arguments are needed:");
			System.out.println("imagefile inputmessagefile outputfile");
			System.out
					.println("imagefile and inputmessagefile are source files");
			System.out
					.println("outputfile is the destination file where the massage will be hidden");
			System.exit(0);
		}

		Stegafy stegafy = new Stegafy();

		// Loop through the first two command-line args and make sure the file
		// exists
		if ((args.length > 2) && (!args[2].equalsIgnoreCase("-t"))) {
			for (int i = 0; i < 2; i++) {
				if (!stegafy.fileExists(args[i])) {
					System.out.println("Unable to perform operation as "
							+ args[i] + " does not exist.");
					System.exit(0);
				}
			}
		}

		// Loop through the first command-line args and make sure the file
		// exists
		else if ((args.length == 2)
				|| ((args.length == 3) && (args[2] == "-t"))) {
			for (int i = 0; i < 1; i++) {
				if (!stegafy.fileExists(args[i])) {
					System.out.println("Unable to perform operation as "
							+ args[i] + " does not exist.");
					System.exit(0);
				}
			}

		}
		// Check if the output file exists. If so, then query the user for an
		// overwrite or not
		if (stegafy.fileExists(args[args.length - 1])) {
			if (!stegafy.overWriteFile(args[args.length - 1])) {
				System.out.println("Operation not performed.");
				System.exit(0);
			}
		}
		
		if((args.length > 2) && (args[2].equalsIgnoreCase("-t"))){
			if (stegafy.fileExists(args[args.length - 2])) {
				if (!stegafy.overWriteFile(args[args.length - 2])) {
					System.out.println("Operation not performed.");
					System.exit(0);
				}
			}
		}

		// checks to make sure input image is bit map format
		if (!stegafy.determineIfBMPFile(args[0])) {

			System.out.println("Image file must be in bit map format (*.bmp)");
			System.exit(0);
		}

		// determines if input file is bit map format
		if ((args.length > 2) && (args[2] != "-t")
				&& (stegafy.determineIfBMPFile(args[1]) == true)) {

			BufferedImage messageImage = null;
			BufferedImage inputImage = null;
			BufferedImage outputImage = null;

			messageImage = stegafy.readInImageFile(args[1]);
			inputImage = stegafy.readInImageFile(args[0]);

			// gets input image height and width
			inputImageHeight = inputImage.getHeight();
			inputImageWidth = inputImage.getWidth();

			// gets message image height and width
			messageImageHeight = messageImage.getHeight();
			messageImageWidth = messageImage.getWidth();

			// Checks image to make sure that the message image is not bigger
			// than the file it must be embedded in
			if (messageImageHeight > inputImageHeight) {
				System.out
						.println("The message image height is too large. Use a smaller massage image or a larger input image");
			}

			if (messageImageWidth > inputImageWidth) {
				System.out
						.println("The message image width is too large. Use a smaller massage image or a larger input image");
			}

			BufferedImage tempImage = inputImage;

			// changes the LSB of blue values to 1 or 0
			outputImage = stegafy.changeOutputBlueValueForBMP(messageImage,
					inputImage, tempImage);

			stegafy.writeImageFile(outputImage, args[2]);
		}

		// Determines if input file is text format
		if (stegafy.determineIfTXTFileSecond(args[1]) && !(args[2].equalsIgnoreCase("-t")) ) {

			StringBuilder content = new StringBuilder();

			// reads in text file
			try {
				// uses buffering, reading one line at a time
				BufferedReader input = new BufferedReader(new FileReader(
						args[1]));
				try {
					String line = null;

					while ((line = input.readLine()) != null) {
						content.append(line);
					}
				} finally {
					input.close();
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}

			// String of characters from text file
			String message = content.toString();

			BufferedImage inputImage = null;
			BufferedImage outputImage = null;

			// Reads in image file and assigns to both input and output
			inputImage = stegafy.readInImageFile(args[0]);
			outputImage = stegafy.readInImageFile(args[0]);

			// gets input image height and width
			inputImageHeight = inputImage.getHeight();
			inputImageWidth = inputImage.getWidth();

			// Checks to make sure the number of pixels is less than the total
			// amount of binary digits put in. subtracts for needed stop : #EOM#
			if ((message.length()*8) > ((inputImageHeight * inputImageWidth) - 40)) {
				System.out
						.println("The text message is too large to be hidden in the image. Use a smaller text file or a larger image.");
				System.exit(0);
			}
			
			StringBuilder bitValue = new StringBuilder();

			for (int i = 0; i < message.length(); i++) {

				char myChar = message.charAt(i);
				int orVal = 128;
				int bitVal = 0;
				for (int j = 0; j < 8; j++) {
					// Get the actual bit value
					bitVal = myChar & orVal;
					bitVal /= orVal;
					// Set the orVal for the next time through the loop to get
					// the next bitVal
					orVal /= 2;
					bitValue.append(bitVal);
				}
			}
			
			//Appends the stop meesage to String
			bitValue.append(BINARYSTOP);
			System.out.println(bitValue);
			String messagebitValue = bitValue.toString();
			int bitMessageLength = bitValue.length();
			int dimension = (inputImageHeight * inputImageWidth) - 40;
			System.out.println(dimension);



			outputImage = stegafy.changeOutputBlueValueForTXT(messagebitValue, inputImage,
					outputImage);
			stegafy.writeImageFile(outputImage, args[2]);
		}

		// if only two arguments are passed in assumes decoding bitmap file
		if (args.length == 2) {

			BufferedImage inputImage = null;
			BufferedImage outputImage = null;

			// reads in image files
			inputImage = stegafy.readInImageFile(args[0]);
			outputImage = stegafy.readInImageFile(args[0]);

			for (int i = 0; i < inputImage.getHeight(); i++) {

				for (int j = 0; j < inputImage.getWidth(); j++) {
					outputImage.setRGB(j, i, 16777215);
					int pixelInt = inputImage.getRGB(j, i);

					byte abyte = (byte) (pixelInt & 0xFF);
					String stringbyte = Integer.toBinaryString(abyte);
					char lastInt = stringbyte.charAt(stringbyte.length() - 1);

					if (lastInt == '0') {
						outputImage.setRGB(j, i, 0);
					}
				}
			}

			stegafy.writeImageFile(outputImage, args[1]);
		}

		
		//checks for text file as second argument, encodes message into image
		if ((args.length > 2) && (args[2].equalsIgnoreCase("-t"))) {
			

			BufferedImage inputImage = null;
			BufferedImage outputImage = null;

			//new StringBuilder object
			StringBuilder hiddenMessage = new StringBuilder();
			StringBuilder hiddenOutputMessage = new StringBuilder();
			
			// reads in image files
			inputImage = stegafy.readInImageFile(args[0]);

			for (int i = 0; i < inputImage.getHeight(); i++) {

				for (int j = 0; j < inputImage.getWidth(); j++) {
					
					int pixelInt = inputImage.getRGB(j, i);

					//int blue =  (pixelInt & 0xFF);
					String stringbyte = Integer.toBinaryString(pixelInt);
					
					char lastInt = stringbyte.charAt(stringbyte.length() - 1);

					if (lastInt == '0') {
						hiddenMessage.append("0");
					}
					else if(lastInt == '1'){
						hiddenMessage.append("1");
					}
					
					
					if(stegafy.checkForStop(hiddenMessage)){
						return;
					}
				}
			}

			String temp = hiddenMessage.toString();
			System.out.print(hiddenMessage);
			for(int i=0; i<temp.length(); i=i+8){
				
				int val = Integer.parseInt(temp.substring(i, i+8));
				char myChar = (char)val;
				hiddenOutputMessage.append(myChar);
			}
			
			temp = hiddenOutputMessage.toString();
			//System.out.println(temp);
			try {
				BufferedWriter out = new BufferedWriter(new FileWriter(args[1]));
				out.write(temp);
				out.close();
				}
				catch (IOException e)
				{
				System.out.println("Exception: Failed to write file");

				}
		}

	}

	private boolean checkForStop(StringBuilder hiddenMessage) {
	
		String isStop = "";
		
		if (hiddenMessage.length()>41){
		isStop = hiddenMessage.substring( (hiddenMessage.length()-41), (hiddenMessage.length()-1) );
		}
		if (isStop.equalsIgnoreCase(BINARYSTOP)){
			return true;
		}
		
		return false;
	
		
	}

	/**
	 * Checks to see if a file exists
	 * 
	 * Precondition: filename != null and filename.legnth != 0
	 * 
	 * @param filename
	 *            filename to see if it exists
	 * 
	 * @return Whether file exists or not ? true:false
	 */
	public boolean fileExists(String filename) {
		// checks for null
		if (filename == null) {
			throw new IllegalArgumentException("filename is null.");
		}
		// checks for empty string
		if (filename.length() <= 0) {
			throw new IllegalArgumentException("filename is empty.");
		}

		File file = new File(filename);

		return file.exists();

	}

	/**
	 * Queries the user to see if they want to overwrite a file.
	 * 
	 * Precondition: filename != null and filename != ""
	 * 
	 * @param filename
	 *            the file to overwrite
	 * @return true = overwrite; false = do not overwrite
	 */
	public boolean overWriteFile(String filename) {

		if (filename == null) {
			throw new IllegalArgumentException("filename is null.");
		}

		if (filename.length() <= 0) {
			throw new IllegalArgumentException("filename is empty.");
		}

		// Output the query message and get the user's response
		System.out
				.print(filename
						+ " is already present. Do you wish to overwrite the file?(y/n)");

		// new scanner object
		Scanner scan = new Scanner(System.in);
		String inData = scan.next();

		if (inData.equalsIgnoreCase("y"))
			return true;
		else
			return false;

	}

	/**
	 * Reads in an image file by the given filename
	 * 
	 * Precondition: filename != null && filename != ""
	 * 
	 * @param filename
	 *            of file to get
	 * @return Image that has been read in
	 */
	public BufferedImage readInImageFile(String filename) {

		if (filename == null) {
			throw new IllegalArgumentException("filename is null.");
		}

		if (filename.length() <= 0) {
			throw new IllegalArgumentException("filename is empty.");
		}

		BufferedImage tempImage = null;

		try {
			// Read from an input stream
			InputStream is = new BufferedInputStream(new FileInputStream(
					filename));
			tempImage = ImageIO.read(is);
			is.close();
		} catch (IOException iox) {
			System.out.println(iox.getMessage());
			iox.printStackTrace();
		}

		return tempImage;

	}

	/**
	 * Write the outPut Image to the given filename outputFile
	 * 
	 * @param outputImage
	 *            Image to write out
	 * @param outputFile
	 *            name of file to write out
	 */
	public void writeImageFile(BufferedImage outputImage, String outputFile) {
		try {
			File file = new File(outputFile);
			ImageIO.write(outputImage, "bmp", file);
		} catch (IOException iox) {
			System.out.println(iox.getMessage());
			iox.printStackTrace();
		}
	}

	/**
	 * Checks if first command line argument has file extension .bmp
	 * 
	 * Precondition: filename != null and filename != ""
	 * 
	 * @param filename
	 *            file name to check
	 * 
	 * @return true if .bmp; false if not
	 * 
	 */
	public boolean determineIfBMPFile(String filename) {

		if (filename == null) {
			throw new IllegalArgumentException("filename is null.");
		}

		if (filename.length() <= 0) {
			throw new IllegalArgumentException("filename is empty.");
		}

		if (!filename.substring(filename.length() - 4).equalsIgnoreCase(".bmp")) {

			return false;

		}

		return true;
	}

	/**
	 * Determines file is a text file format
	 * 
	 * Precondition: filename != null and filename!+ ""
	 * 
	 * @param filename
	 *            to check
	 * 
	 * @return true if is text false if is not text
	 */
	public Boolean determineIfTXTFileSecond(String filename) {

		if (filename == null) {
			throw new IllegalArgumentException("filename is null.");
		}

		if (filename.length() <= 0) {
			throw new IllegalArgumentException("filename is empty.");
		}

		if (filename.substring(filename.length() - 4).equalsIgnoreCase(".txt")) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Changes the blue value of each pixel to correspond with the hidden
	 * message
	 * 
	 * Precondition: messageImage != null; inputImage != null; outputImage !=
	 * null;
	 * 
	 * @param messageImage
	 * @param inputImage
	 * @param outputImage
	 * @return
	 */
	public BufferedImage changeOutputBlueValueForBMP(
			BufferedImage messageImage, BufferedImage inputImage,
			BufferedImage outputImage) {

		// checks for nulls
		if (messageImage == null) {
			throw new IllegalArgumentException("mesaageImage is null.");
		}
		if (inputImage == null) {
			throw new IllegalArgumentException("inputImage is null.");
		}
		if (outputImage == null) {
			throw new IllegalArgumentException("outputImage is null.");
		}

		for (int i = 0; i < messageImage.getHeight(); i++) {

			for (int j = 0; j < messageImage.getWidth(); j++) {

				int pixelInt = inputImage.getRGB(j, i);
				int messagePixel = messageImage.getRGB(j, i);

				// checks last bit of pixel integer to see if will interfere
				// with drawing out the hidden message
				if (pixelInt == 0xFFFFFFFE) {
					int tempInt = pixelInt;
					tempInt &= 0xFFFFFFFD;
					outputImage.setRGB(j, i, tempInt);
				}

				if (messagePixel != -1) {
					int tempInt = pixelInt;
					byte lowByte = (byte) (tempInt & 0xFF);
					// System.out.print(Integer.toBinaryString(lowByte) + ", ");
					tempInt &= 0xFFFFFFFE;
					lowByte = (byte) (tempInt & 0xFF);
					// System.out.println(Integer.toBinaryString(lowByte) +
					// "; ");

					outputImage.setRGB(j, i, tempInt);
				} else {
					int tempInt = pixelInt;
					tempInt |= 1;
					outputImage.setRGB(j, i, tempInt);
				}
			}

		}
		return outputImage;
	}

	/**
	 * Changes the blue value of each pixel to correspond with the hidden
	 * message
	 * 
	 * Precondition: messageImage != null || != ""; inputImage != null;
	 * outputImage != null;
	 * 
	 * @param message
	 * @param inputImage
	 * @param outputImage
	 * @return
	 */
	public BufferedImage changeOutputBlueValueForTXT(String message,
			BufferedImage inputImage, BufferedImage outputImage) {

		// checks for nulls
		if (message == null) {
			throw new IllegalArgumentException("message is null.");
		}

		if (message.length() < 1) {
			throw new IllegalArgumentException("message is empty.");
		}

		if (inputImage == null) {
			throw new IllegalArgumentException("inputImage is null.");
		}
		if (outputImage == null) {
			throw new IllegalArgumentException("outputImage is null.");
		}
		for (int i = 0; i < outputImage.getHeight(); i++) {
			for (int j = 0; j < outputImage.getWidth(); j++) {

				for (int k = 0; k < message.length(); k++) {

					char charTemp = message.charAt(k);

					int pixelInt = inputImage.getRGB(j, i);

					if (charTemp =='0') {
						int tempInt = pixelInt;
						tempInt &= 0xFFFFFFFE;
						outputImage.setRGB(j, i, tempInt);
					} else if (charTemp == '1'){
						int tempInt = pixelInt;
						tempInt |= 1;
						outputImage.setRGB(j, i, tempInt);
					}
				}

			}
		}
		return outputImage;
	}
}
