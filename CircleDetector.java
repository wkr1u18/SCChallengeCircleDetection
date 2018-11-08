import javax.imageio.ImageIO;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Scanner;

public class CircleDetector {
    public File storedFile = null;
    public BufferedImage myImage = null;
    public BufferedImage tempImage = null;
    public int[][] edgeColors = null;
    public static void main(String [] args) {
    	System.out.println("Hi");
        CircleDetector myDetector = new CircleDetector();
        myDetector.readImage("image.jpg");
        myDetector.convertToGreyscale();
        myDetector.applySobel();
        myDetector.detectCircles(100, myDetector.myImage.getHeight()/2, 180);
        myDetector.saveImage("circle.jpg");
        System.out.println("finished");

    }

    //Reads image into myImage object
    void readImage(String fileName) {
        try {
            storedFile = new File(fileName);
            myImage = ImageIO.read(storedFile);
        }
        catch (IOException ioe) {
            System.out.println(ioe);
        }
    }

    //Saves image into myImage object
    void saveImage(String fileName) {
        try {
            File fileToSave = new File(fileName);
            ImageIO.write(myImage,"jpg",fileToSave);
        }
        catch (IOException ioe) {
            System.out.println(ioe);
        }
    }

    //Converts stored BufferedImage object into greyscale picture
    void convertToGreyscale() {
        //get image width and height
        int width = myImage.getWidth();
        int height = myImage.getHeight();
        tempImage = new BufferedImage(width, height, 1);

        for (int y = 0; y<height; y++) {
            for (int x = 0; x < width; x++) {
                //Get pixel information
                int p = myImage.getRGB(x, y);

                //Extract the RGB and alpha information
                int a = (p >> 24) & 0xff;
                int r = (p >> 16) & 0xff;
                int g = (p >> 8) & 0xff;
                int b = p & 0xff;

                //Calculate RGB average
                int avg = (r + g + b) / 3;

                //Alter the pixel code and set it
                p = (a << 24) | (avg << 16) | (avg << 8) | avg;
                tempImage.setRGB(x, y, p);
            }
        }

    }

    //Applies Soblel operator to picture
    void applySobel() {
        int width = tempImage.getWidth();
        int height = tempImage.getHeight();
        edgeColors = new int[width][height];
        int pixelX, pixelY, pixel;
        int[][] pixelMatrix=new int[3][3];
        int maxGradient = -1;
        for(int y = 1 ; y<=height-2; y++) {
            for(int x = 1; x<=width-2; x++) {
                pixelMatrix[0][0]=new Color(tempImage.getRGB(x-1,y-1)).getRed();
                pixelMatrix[0][1]=new Color(tempImage.getRGB(x-1,y)).getRed();
                pixelMatrix[0][2]=new Color(tempImage.getRGB(x-1,y+1)).getRed();
                pixelMatrix[1][0]=new Color(tempImage.getRGB(x,y-1)).getRed();
                //pixelMatrix[1][1]=new Color(myImage.getRGB(x,y)).getRed();
                pixelMatrix[1][2]=new Color(tempImage.getRGB(x,y+1)).getRed();
                pixelMatrix[2][0]=new Color(tempImage.getRGB(x+1,y-1)).getRed();
                pixelMatrix[2][1]=new Color(tempImage.getRGB(x+1,y)).getRed();
                pixelMatrix[2][2]=new Color(tempImage.getRGB(x+1,y+1)).getRed();
                int result = (int) convolution(pixelMatrix);
                if (result>maxGradient) {
                	maxGradient = result;
                }
                edgeColors[x][y]=result;
            }
        }
        double scale = 255.0 / maxGradient;
        for(int y = 1 ; y<=height-2; y++) {
        	for(int x = 1 ; x<=width-2; x++) {
        		int color = (int) (edgeColors[x][y]*scale);
        		tempImage.setRGB(x, y, 0xff000000 | (color << 16) | (color << 8) | color);
        	}
        }
    }
    
    void detectCircles(int min, int max, int treshold) {
        int width = tempImage.getWidth();
        int height = tempImage.getHeight();
    	for(int r = min; r<=max; r++) {
    		for(int x = min; x<=width-min; x++) {
    			for(int y = min; y<= height-min; y++) {
    				if(this.checkCircle(x,y,r,treshold)) {
    					this.drawCircle(x, y, r);
    				}
    			}
    		}
    	}
    }
    public static double convolution(int[][] pixelMatrix){

        int gy=(pixelMatrix[0][0]*-1)+(pixelMatrix[0][1]*-2)+(pixelMatrix[0][2]*-1)+(pixelMatrix[2][0])+(pixelMatrix[2][1]*2)+(pixelMatrix[2][2]*1);
        int gx=(pixelMatrix[0][0])+(pixelMatrix[0][2]*-1)+(pixelMatrix[1][0]*2)+(pixelMatrix[1][2]*-2)+(pixelMatrix[2][0])+(pixelMatrix[2][2]*-1);
        return Math.sqrt(Math.pow(gy,2)+Math.pow(gx,2));
    }
    
    public void drawCircle(int x0, int y0, int r) {
    	
    	int x = r-1;
    	int y = 0;
    	int dx = 1;
    	int dy = 1;
    	int err = dx - (r << 1);

    	while (x >= y)
    	{
    	    myImage.setRGB(x0 + x, y0 + y, 0xFF0000);
    	    myImage.setRGB(x0 + y, y0 + x, 0xFF0000);
    	    myImage.setRGB(x0 - y, y0 + x, 0xFF0000);
    	    myImage.setRGB(x0 - x, y0 + y, 0xFF0000);
    	    myImage.setRGB(x0 - x, y0 - y, 0xFF0000);
    	    myImage.setRGB(x0 - y, y0 - x, 0xFF0000);
    	    myImage.setRGB(x0 + y, y0 - x, 0xFF0000);
    	    myImage.setRGB(x0 + x, y0 - y, 0xFF0000);

    	    if (err <= 0)
   	     	{
    	    	y++;
   	         	err += dy;
   	         	dy += 2;
   	     	}
   	         	     
   	     	if (err > 0)
   	     	{
   	     		x--;
   	     		dx += 2;
   	     		err += dx - (r << 1);
   	     	}
    	}
    }
    public boolean checkCircle(int x0, int y0, int r, int treshold) {
    	int x = r-1;
    	int y = 0;
   	    int dx = 1;
   	    int dy = 1;
   	    int err = dx - (r << 1);

   	    while (x >= y)
   	    {
   	    	try {
   	    		if((new Color(tempImage.getRGB(x0 + x, y0 + y)).getRed()<treshold)&(new Color(tempImage.getRGB(x0 + y, y0 + x)).getRed()<treshold)&(new Color(tempImage.getRGB(x0 - y, y0 + x)).getRed()<treshold)&(new Color(tempImage.getRGB(x0 - x, y0 + y)).getRed()<treshold)&(new Color(tempImage.getRGB(x0 - x, y0 - y)).getRed()<treshold)&(new Color(tempImage.getRGB(x0 - y, y0 - x)).getRed()<treshold)&(new Color(tempImage.getRGB(x0 + y, y0 - x)).getRed()<treshold)&(new Color(tempImage.getRGB(x0 + x, y0 - y)).getRed()<treshold)) {
   	    			return false;
   	    		}
   	    	}
   	    	catch (Exception e) {
   	    		return false;
   	    	}
   	    	

   	        if (err <= 0)
   	        {
   	            y++;
   	            err += dy;
   	            dy += 2;
   	        }
   	        
   	        if (err > 0)
   	        {
   	            x--;
   	            dx += 2;
   	            err += dx - (r << 1);
   	        }
   	    }
   	    return true;
   }
}