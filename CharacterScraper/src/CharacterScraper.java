import static io.github.bonigarcia.wdm.DriverManagerType.CHROME;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;

import org.jsoup.*;
import org.jsoup.nodes.*;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

public class CharacterScraper{
	private static String[] races = {"Aasimar","Dragonborn","Dwarf","Elf","Firbolg","Gnome","Goblin","Goliath","Halfling","Half-Elf","Half-Orc","Human","Kenku","Lizardfolk","Medusa","Orc","Tabaxi","Tiefling","Triton","Troglodyte"};
	private static String[] classNames = {"Learned","Lesser Nobility","Professional","Working Class","Martial","Underclass","Entertainer"};
	private static String destPath = "";
	private static JLabel error = new JLabel("Result: ");	
	public static void main( String[] args ) throws IOException, InterruptedException
    {
		
		JFrame f = new JFrame("Data Stealer 1.1  :^)");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.pack();
        f.setVisible(true);
        
        JButton b = new JButton("Submit");    
		b.setBounds(100,100 + 250,140, 40);
			
		JLabel label = new JLabel("Input:");		
		label.setBounds(10, 10 + 250, 50, 100);
		
		JLabel image = new JLabel(new ImageIcon("src/instr.png"));
		image.setBounds(25, 25, 650, 250);
					
		JTextField textfield = new JTextField();
		textfield.setBounds(60, 50 + 250, 590, 30);
		
		error.setBounds(260 ,80 + 250, 300, 60);
						
		f.add(label);
		f.add(error);
		f.add(image);
		f.add(textfield);
		f.add(label);
		f.add(b);    
		f.setSize(715,450);    
		f.setLayout(null);    
		f.setVisible(true);    
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);   

		final JFileChooser fc = new JFileChooser();
    	fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    	while(true) {
    		try {
		    	fc.showOpenDialog(f);   	
		    	destPath = fc.getSelectedFile().getAbsolutePath();
		    	error.setText("Output Destination: " + destPath);
    			f.update(f.getGraphics());
    			break;
    		}catch(NullPointerException e) {
    			error.setText("Path Error");
    			f.update(f.getGraphics());
    		}
    	}
    	
    	WebDriverManager.getInstance(CHROME).setup();
    	WebDriver driver = new ChromeDriver();
    	
    	f.addWindowListener(new WindowAdapter() {
			  public void windowClosing(WindowEvent we) {
				driver.quit();
			    System.exit(0);
			  }
			
		});
    	
    	String ref = "http://www.npcgenerator.com/";
    	
    	b.addActionListener(new ActionListener() {
	        
			@Override
			public void actionPerformed(ActionEvent arg0) {
				try {
					String inp = textfield.getText();
					inp = inp.replace(" ", "");
					inp = inp.replace("\n", "");
					String[] split = inp.split(",");
					SXSSFWorkbook wb = getNewWorkBook();
					tryGetDocument(ref, driver);
					int rowNum = 1;
					for(String s: split) {
						String[] first = s.split(";");
						String[] pair = first[0].split(":");
						String[] percentages = first[1].split(":");			
						int race = findRace(pair[0].toLowerCase().trim());
						int num = 0;
						num = Integer.parseInt(pair[1]);
						double[] percen = new double[7];
						for(int n = 0; n < percentages.length; n++) {
							percen[n] = ((double)Integer.parseInt(percentages[n].trim())) / 100.0;
						}
						int[] cT = {0,0,0,0,0,0,0};
						if(race != -1)						
							for(int i = 0; i < num; i++){
								int socialClass = getClassIndex(percen);
								cT[socialClass]++;
								writeRow(
										wb.getSheetAt(0),
										getCharacter(driver,race,socialClass),
										rowNum
								);
								String res = "Getting " + races[race] + " number " + i + " / " + num + "   :  " + cT[0] + "| "+cT[1]+"| "+cT[2]+"| "+cT[3]+"| "+cT[4]+"| "+cT[5]+"| "+cT[6];
								/*res += "Learned:\t\t" + cT[0]  + "\\n";
								res += "Lesser Nobility:\t\t" + cT[1]  + "\\n";
								res += "Professional:\t\t" + cT[2]  + "\\n";
								res += "Working Class:\t\t" + cT[3]  + "\\n";
								res += "Martial:\t\t" + cT[4]  + "\\n";
								res += "Underclass:\t\t" + cT[5]  + "\\n";
								res += "Entertainer:\t\t" + cT[6]  + "\\n";*/
								error.setText(res);		
								f.update(f.getGraphics());
								rowNum++;
							}
						else {
							error.setText("Error: Race Name Error");
							f.update(f.getGraphics());
						}		
					}
					String fileName = destPath + "\\ResultCharacterSet.xlsx";
			    	try {
			        	FileOutputStream excelOutputStream = new FileOutputStream(fileName);
						wb.write(excelOutputStream);
						excelOutputStream.close();
					} catch (IOException e) {
						error.setText("Error: Excel File Already Open");
						f.update(f.getGraphics());
					}			    
			    	error.setText("Success: at " + destPath + "\\ResultCharacterSet.xlsx");
			    	f.update(f.getGraphics());
				}catch(NumberFormatException e) {
					error.setText("Error: Number Input Error");
					f.update(f.getGraphics());
				}catch(IndexOutOfBoundsException e) {
					error.setText("Error: Delim Input Error");
					f.update(f.getGraphics());
				}catch(WebDriverException e) {
					driver.quit();
				    System.exit(0);
				}
			} 	
		});  	
    }
	
	private static int findRace(String s) {
		int num = 0;
		for(String r: races) {
			if(r.toLowerCase().equals(s))
				return num;
			num++;
		}
		return -1;
	}
	
	private static int getClassIndex(){
		double num = Math.random();
		if(num < .45)
			return 2;
		if(num < .90)
			return 3;
		if(num < .92)
			return 0;
		if(num < .94)
			return 1;
		if(num < .96)
			return 4;
		if(num < .98)
			return 5;
		return 6;
	}
	
	private static int getClassIndex(double percentages[]){
		double num = Math.random();
		double accum = 0.0;
		for(int i = 0; i < 7; i++) {
			accum += percentages[i];
			if(num <= accum)
				return i;
		}
		
		return (int)(Math.random() * 6.9999);
	}
	
	private static String[] getCharacter(WebDriver driver, int race, int classN){
		String[] info = new String[8];
        List<WebElement> dds = driver.findElements(By.className("form-group"));
        for(WebElement sV: dds) {
        	String sVal = sV.findElement(By.className("control-label")).getText().toLowerCase().trim();
        	WebElement d = sV.findElement(By.className("form-control"));
        	switch(sVal) {
        		case "race":  			
        			Select raceDd = new Select(d);
        			raceDd.selectByValue(Integer.toString(race));
        			break;
        		case "sex": 
        			Select d1 = new Select(d);
        			d1.selectByValue("random");
        			break;
        		case "alignment": 
        			Select d2 = new Select(d);
        			d2.selectByValue("random");
        			break;
        		case "occupation": 
        			Select d3 = new Select(d);
        			d3.selectByValue("1");
        			break;
        		case "plot hooks": 
        			Select profDd = new Select(d);
        			profDd.selectByValue("random");
        			break;
        		case "social class": 
        			Select classD = new Select(d);
        			classD.selectByValue(Integer.toString(classN));
        			break;
        		case "profession": 
        			Select d6 = new Select(d);
        			d6.selectByValue("random");
        			break;       	
        	}
        }
        rest();
        driver.findElement(By.className("generate-button")).click();
        Document res = Jsoup.parse(driver.getPageSource());
        
        ArrayList<Element> panels = res.getElementsByClass("panel-body");
        info[5] = panels.get(1).text().replace("#", "");
        info[6] = panels.get(2).text().replace("#", "");
        info[7] = panels.get(6).text().replace("#", "");
        try {
        info[0] = info[5].substring(0, info[5].indexOf(" is ")).trim();
        if(info[5].indexOf(races[race].toLowerCase()) != -1)
        	info[1] = info[5].substring(info[5].indexOf("year old ") + 9, info[5].indexOf(races[race].toLowerCase())).trim() + " " + races[race];
        else 
        	info[1] = info[5].trim().split(" ")[8];       
        info[1] = info[1].replace("female", "").replace("male", "").trim();
        info[2] = info[5].substring(info[5].indexOf(" is a ") + 6, info[5].indexOf(" year ")).trim();
        info[3] = classNames[classN];
        info[4] = info[5].substring(info[5].indexOf(races[race].toLowerCase()) + 1 + races[race].length(), info[5].indexOf('.')).trim();
        } catch(java.lang.StringIndexOutOfBoundsException e) {
        	System.out.println(info[5]);
        }
		return info;
	}
	
	private static void writeRow(SXSSFSheet s, String[] info, int rowNum) {
		Row r = s.createRow(rowNum);	  
	    r.createCell(0).setCellValue(info[0]);
	    r.createCell(1).setCellValue(info[1]);
	    r.createCell(2).setCellValue(info[2]);
	    r.createCell(3).setCellValue(info[3]);
	    r.createCell(4).setCellValue(info[4]);
	    r.createCell(5).setCellValue(info[5]);
	    r.createCell(6).setCellValue(info[6]);
	    r.createCell(7).setCellValue(info[7]);
	}
	
	private static void tryGetDocument(String absRef, WebDriver driver)
    {
    	boolean retry = true;
    	long timeout = System.currentTimeMillis();
    	while(retry) {  	
	    	try {
	    		driver.get(absRef);
	    		retry = false;
	    	}catch(TimeoutException e) {
	    		retry = true;
	    		error.setText("Retrying Connection, Timeout In: " + (System.currentTimeMillis() - timeout) + "ms");
	    	}
	    	if(System.currentTimeMillis() - timeout > 60000)
	    		break;
    	}
    	rest();
    }
	
	private static void rest()
    {
    	long targetTime = (long)(Math.random() * 200 + 200) + System.currentTimeMillis();
    	while(targetTime > System.currentTimeMillis()){
    	}
    }
	
   private static SXSSFWorkbook getNewWorkBook(){
		SXSSFWorkbook workbook = new SXSSFWorkbook();	  
		SXSSFSheet characters = workbook.createSheet("Characters");
	    Row header = characters.createRow(0);	  
	    header.createCell(0).setCellValue("Name");
	    header.createCell(1).setCellValue("Race");
	    header.createCell(2).setCellValue("Age");
	    header.createCell(3).setCellValue("Class");
	    header.createCell(4).setCellValue("Profession");
        header.createCell(5).setCellValue("Description");
        header.createCell(6).setCellValue("Backstory");
        header.createCell(7).setCellValue("Hook");
        return workbook;
   }
}
