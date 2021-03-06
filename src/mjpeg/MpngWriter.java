package mjpeg;
import java.io.*;								//Random access file writer
import java.util.*;							//Vector
import com.keypoint.*;					//pngCodec
import java.awt.image.BufferedImage;
import java.awt.Dimension;
public class MpngWriter{

		long frameNo = 0;
		Vector<Long> frames;
		Vector<Integer> fsizes;
		long movi;
		long[] frameP	=null;	//Frame pointer
		long[] position = null;	//File position pointer
		RandomAccessFile videoFile	=null;	//Video file to write to

		int height;
		int width;
		long frameCount;
		
		
		//Constructor
		
		public MpngWriter(String fileName,Dimension size) throws Exception{
			this.width = size.width;
			this.height = size.height;
			frameP = new long[3];
			position = new long[3];
			frames = new  Vector<Long>();
			fsizes = new Vector<Integer>();
			frameCount = 0;
			videoFile = new RandomAccessFile(fileName,"rw");	
		}
		
		
		public MpngWriter(String fileName,int width,int height) throws Exception{
			this.width = width;
			this.height = height;
			frameP = new long[3];
			position = new long[3];
			frames = new  Vector<Long>();
			fsizes = new Vector<Integer>();
			frameCount = 0;
			videoFile = new RandomAccessFile(fileName,"rw");			
		}

		public void writeHeader() throws Exception{
			String text = "RIFF";
			videoFile.write(text.getBytes("ISO-8859-1")); //Riff
			videoFile.write(intToByteArray(0)); //File size in bytes-8

			//AVI
			text = "AVI ";
			videoFile.write(text.getBytes("ISO-8859-1")); //AVI
			text = "LIST";
			videoFile.write(text.getBytes("ISO-8859-1")); //LIST
			videoFile.write(intToByteArray(192));	//list size (header size-12)
			text = "hdrl";
			videoFile.write(text.getBytes("ISO-8859-1")); //main header
			text = "avih";
			videoFile.write(text.getBytes("ISO-8859-1")); //avi header
			videoFile.write(intToByteArray(14*4));	//avih size in bytes
			
			//AVI header data
			videoFile.write(intToByteArray(40000));	//usec per frame
			videoFile.write(intToByteArray(25*3*width*height));	//bytes per sec
			videoFile.write(intToByteArray(0));	//reserved
			videoFile.write(intToByteArray(2320));	//flags (none required for raw)
			frameP[0] = videoFile.getFilePointer();
			videoFile.write(intToByteArray(0));		//total frames (none required for raw)
			videoFile.write(intToByteArray(0));		//initial frames (0 for non-interleaved)
			videoFile.write(intToByteArray(1));		//No. of streams
			videoFile.write(intToByteArray(width*height*3));		//Buffer size
			videoFile.write(intToByteArray(width));		//Width
			videoFile.write(intToByteArray(height));		//Height
			videoFile.write(intToByteArray(0));		//Reserved[4] set to zero
			videoFile.write(intToByteArray(0));		//Reserved[4] set to zero
			videoFile.write(intToByteArray(0));		//Reserved[4] set to zero
			videoFile.write(intToByteArray(0));		//Reserved[4] set to zero
			//AVI header written
			
			//Stream header
			text = "LIST";
			videoFile.write(text.getBytes("ISO-8859-1"));
			videoFile.write(intToByteArray(116));	//list size in bytes
			text = "strl";		//Stream list
			videoFile.write(text.getBytes("ISO-8859-1"));		//Stream list
			text = "strh";		//Stream header
			videoFile.write(text.getBytes("ISO-8859-1"));		//videoFileStream header
			videoFile.write(intToByteArray(56));	//videoFileStream header size in bytes
			//Stream header data
			text = "vids";		//Stream header
			videoFile.write(text.getBytes("ISO-8859-1"));		//videoFileStream header
			text = "MPNG";		//Stream handler
			videoFile.write(text.getBytes("ISO-8859-1"));		//Stream handler
			videoFile.write(intToByteArray(0));	//Flags
			videoFile.write(intToByteArray(0));	//Priority none needed for raw..
			videoFile.write(intToByteArray(0));		//Language needed for raw..
			//InitialFrames not used -> no audio...
			videoFile.write(intToByteArray(1));	//dwScale
			videoFile.write(intToByteArray(25));	//dwRate
			videoFile.write(intToByteArray(0));	//dwStart
			frameP[1] = videoFile.getFilePointer();

			videoFile.write(intToByteArray(0));	//dwLength
			videoFile.write(intToByteArray(0));	//dwBuffer
			videoFile.write(intToByteArray(-1));	//dwQuality
			videoFile.write(intToByteArray(0));	//dwSampleSize
			videoFile.write(intToByteArray(0));	//rcFrame
			videoFile.write(intToByteArray(0));	//rcFrame
			//Stream header data written
			text = "strf";		//Stream format
			videoFile.write(text.getBytes("ISO-8859-1"));		//videoFileStream format
			videoFile.write(intToByteArray(40));	//videoFileStream fomat size in bytes
			videoFile.write(intToByteArray(40));	//Format header length = 40
			//format data
			videoFile.write(intToByteArray(width));	//width
			videoFile.write(intToByteArray(height));	//height
			videoFile.write(shortToByteArray((short) 1));	//planes = must be 1
			videoFile.write(shortToByteArray((short) 0));	//bitCount (0 =jpeg tai png)
			text = "MPNG";		//Compression
			videoFile.write(text.getBytes("ISO-8859-1"));		//compression
			videoFile.write(intToByteArray(width*height*3));	//image size in bytes
			videoFile.write(intToByteArray(0));	//xpixels per meter
			videoFile.write(intToByteArray(0));	//ypixels per meter
			videoFile.write(intToByteArray(0));	//ClrUsed
			videoFile.write(intToByteArray(0));	//ClrImportant
			//format data written...
	
			//DATA chunk!!
			text = "LIST";		//MOVI chunk
			videoFile.write(text.getBytes("ISO-8859-1"));		//Movi chunk
			frameP[2] = videoFile.getFilePointer();	//Insert pointer for decoded data
			videoFile.write(intToByteArray(0));	//Decoded Data size!!
	
			movi = videoFile.getFilePointer();
			text = "movi";		//MOVI list name
			videoFile.write(text.getBytes("ISO-8859-1"));		//Movi list name
		}

		
		//Functions 
		public void write_frame(BufferedImage image) throws Exception{
			String text = "00dc";		//MOVI chunk
			position[0] =videoFile.getFilePointer();
			videoFile.write(text.getBytes("ISO-8859-1"));		//Movi chunk
			videoFile.write(intToByteArray(width*height*3));//Movi chunk size place holder....
			position[1] =videoFile.getFilePointer();
			write_PNG(image);
			if ((videoFile.getFilePointer()-position[0])%4 > 0){ //Zero padding
				byte[] padding = new byte[(int) ((videoFile.getFilePointer()-position[0])%4)];
				videoFile.write(padding);
			}
			position[2] =videoFile.getFilePointer();
			videoFile.seek(position[0]+4);
			videoFile.write(intToByteArray((int) (position[2]-position[1])));		//Movi chunk size
			videoFile.seek(position[2]);
			frames.add(position[0]-movi);
			fsizes.add((int) (position[2]-position[1]));
			++frameCount;
		}
		
		public void finalize_mjpeg() throws Exception{

			long currentPosition = videoFile.getFilePointer();
			long idxPos = currentPosition;
			videoFile.seek(movi-4);
			videoFile.write(intToByteArray((int) (currentPosition-movi)));		//Movi chunk size
			videoFile.seek(currentPosition);
			//Write index
			String text = "idx1";		//idx1 chunk
			videoFile.write(text.getBytes("ISO-8859-1"));		//idx1 chunk
			videoFile.write(intToByteArray((int) (frameCount*4*4)));	//index size
			for (int k = 0;k<frameCount;k++){
				text = "00dc";		//idx1 chunk
				videoFile.write(text.getBytes("ISO-8859-1"));		//idx1 chunk
				videoFile.write(intToByteArray((int) 16));	//Key frame flag!!
				videoFile.write(intToByteArray(frames.get(k).intValue()));	//offset from BEGINNING of (thus the +4) movi!!
				videoFile.write(intToByteArray((int) (fsizes.get(k))));		//chunk size
			}
			currentPosition = videoFile.getFilePointer();
			videoFile.seek(4);
			videoFile.write(intToByteArray((int) (currentPosition-8)));		//File size
			videoFile.seek(frameP[0]);

			videoFile.write(intToByteArray((int) frameCount));
			videoFile.seek(frameP[1]);

			videoFile.write(intToByteArray((int) frameCount));		//frames2

			videoFile.seek(frameP[2]);
			videoFile.write(intToByteArray((int) (idxPos-movi)));		//encoded image data amount
			//Index written...
			//Close videoFile
			videoFile.close();
		}
		
		void write_PNG(BufferedImage data) throws Exception
		{
			PngEncoderB encoder = new PngEncoderB(data);
			byte[] compressedData = encoder.pngEncode();
			videoFile.write(compressedData);
		}

		byte[] intToByteArray(int value){
//			return new byte[] {(byte) (value >> 24 & 0xFF),(byte) (value >> 16 & 0xFF),(byte) (value >> 8 & 0xFF),(byte) (value & 0xFF)};
			return new byte[] {(byte) (value & 0xFF),(byte) ((value >> 8) & 0xFF),(byte) ((value >> 16) & 0xFF),(byte) ((value >> 24) & 0xFF)};


		}	
		byte[] shortToByteArray(short value){
//			return new byte[] {(byte) (value >> 8 & 0xFF),(byte) (value & 0xFF)};
			return new byte[] {(byte) (value & 0xFF),(byte) ((value>>8) & 0xFF)};

		}	

		byte[] intArrayToByteArray(int[] arrayIn){
			byte[] arrayOut = new byte[arrayIn.length*4];
			byte[] temp;
			for (int i= 0; i<arrayIn.length; ++i){
				temp = intToByteArray(arrayIn[i]);
				for (int j = 0; j< 4; ++j){
					arrayOut[4*i+j] = temp[j];
				}
			}		
			return arrayOut;
		}


	public static void main(String[] args)
		{
			
			int videon_korkeus =500;
			int videon_leveys = 500;
			//MJPEG valmistelu
			MpngWriter writer = null;
			try{
				writer = new MpngWriter("H:/UserData/winMigrationBU/Deakin/JGREYADJUST/figs/testi.avi",videon_leveys,videon_korkeus);
				writer.writeHeader();
			}catch (Exception err){System.out.println("couldn't open writer");}
	
			//TALLENNETAAN VIDEOKSI...
			//printf("Kuvaa tayttamaan\n");
			int[] data = new int[videon_leveys*videon_korkeus];
			for (int jjj = 0;jjj<videon_korkeus;++jjj){
				for (int iii = 0;iii<videon_leveys;++iii){
					data[iii+jjj*videon_leveys]= ((0xff & (byte) iii)<<8*2) | (0xff & (byte) jjj);	//R
					
					}
			}
			
			BufferedImage buffImg = new BufferedImage(videon_leveys, videon_korkeus, BufferedImage.TYPE_INT_BGR);
			for (int j = 0; j<videon_korkeus;++j){
				for (int i = 0; i<videon_leveys;++i){
					buffImg.setRGB(i,j,data[i+j*videon_leveys]);
				}
			}
			//Kuva valmis
			for (int i = 0;i<100;++i){
					try{
						writer.write_frame(buffImg);	//add frame
					}catch (Exception err){System.out.println("couldn't write frame");}
					System.out.println("Kuva "+i);
			}
			//Done
			try{
				writer.finalize_mjpeg();
			}catch (Exception err){System.out.println("couldn't finalize");}
			System.out.println("Valmista tuli\n");

				 
	}
}

