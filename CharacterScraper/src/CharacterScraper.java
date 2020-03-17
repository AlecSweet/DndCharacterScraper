import static io.github.bonigarcia.wdm.DriverManagerType.CHROME;

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

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
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

public class CharacterScraper{
	private static String destPath = "";
	private static Future curFuture = null;
	private static InputTask curTask = null;
	
	public static void main( String[] args ) throws IOException, InterruptedException
    {
		startJframe();	
    }
	
	private static void startJframe() {
		ExecutorService taskExecutor = Executors.newFixedThreadPool(1);
		
		JFrame f = new JFrame("                                                                                    Literal Malware ʕ •ᴥ•ʔ");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.pack();
        f.setVisible(true);
        
        JButton b = new JButton("Submit");    
		b.setBounds(25,100 + 250, 120, 40);
			
		JLabel label = new JLabel("Input:");		
		label.setBounds(25, 14 + 250, 50, 100);
		Image i = Toolkit.getDefaultToolkit().getImage(ClassLoader.getSystemResource("instr.png"));
		JLabel image = new JLabel(new ImageIcon(i));
		image.setBounds(25, 25, 650, 250);
					
		JTextField textfield = new JTextField("race : 10 ; 0 : 0 : 0 : 0 : 0 : 0 : 0 ,");
		textfield.setBounds(60, 50 + 250, 590, 30);
		
		JLabel error = new JLabel("Result: ");	
		error.setBounds(155 ,90 + 250, 550, 60);
						
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
    	ChromeOptions op = new ChromeOptions();
    	op.addArguments("headless");
    	op.addArguments("--log-level=3");
    	WebDriver driver = new ChromeDriver(op);
 
    	
    	f.addWindowListener(new WindowAdapter() {
			  public void windowClosing(WindowEvent we) {
				taskExecutor.shutdownNow();
				driver.quit();
			    System.exit(0);
			  }
			
		});
    	
   	
    	b.addActionListener(new ActionListener() {        
			@SuppressWarnings("unchecked")
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if(curFuture == null || b.getText().equals("Submit")) {	
					b.setText("Stop");
					curTask = new InputTask(f, error, textfield.getText(), driver, destPath);
					curFuture = taskExecutor.submit(curTask);
					return;
				}else if(curFuture != null && b.getText().equals("Stop")){
					b.setText("Submit");
					curTask.setCanceled(true);
					curFuture.cancel(true);

					textfield.setText("race : 10 ; 0 : 0 : 0 : 0 : 0 : 0 : 0 ,");				
				}
			}			
		});  	
	}
}

class InputTask implements Callable{
	private static final String[] races = {"Aasimar","Dragonborn","Dwarf","Elf","Firbolg","Gnome","Goblin","Goliath","Halfling","Half-Elf","Half-Orc","Human","Kenku","Lizardfolk","Medusa","Orc","Tabaxi","Tiefling","Triton","Troglodyte"};
	private static final String[] classNames = {"Learned","Lesser Nobility","Professional","Working Class","Martial","Underclass","Entertainer"};
	private static final String ref = "http://www.npcgenerator.com/";
	private boolean canceled = false;
	private JFrame f;
	private JLabel error;
	private String text, destPath;
	private WebDriver driver;
	
	public InputTask(JFrame frame, JLabel error, String text, WebDriver driver, String destPath) {
		f = frame;
		this.error = error;
		this.text = text;
		this.driver = driver;
		this.destPath = destPath;
	}
	
	public void setCanceled(boolean t){
		canceled = t;
	}
	
	@Override
	public Integer call() {
		SXSSFWorkbook wb = getNewWorkBook();
		System.out.println("running1");
		try {
			String inp = text;//textfield.getText();
			inp = inp.replace(" ", "");
			inp = inp.replace("\n", "");
			String[] split = inp.split(",");
			tryGetDocument(ref, driver);
			int rowNum = 1;
			for(String s: split) {
				System.out.println("running2");
				String[] first = s.split(";");
				String[] pair = first[0].split(":");
				String[] percentages = first[1].split(":");
				int race = findRace(pair[0].toLowerCase().trim());
				if(percentages.length < 7){
					throw new IllegalArgumentException("Error: Percentages incorrect for " + races[race]);
				}
				int num = 0;
				num = Integer.parseInt(pair[1]);
				double[] percen = new double[7];
				for(int n = 0; n < percentages.length; n++) {
					percen[n] = ((double)Integer.parseInt(percentages[n].trim())) / 100.0;
				}
				int[] cT = {0,0,0,0,0,0,0};
				if(race != -1)						
					for(int i = 0; i < num; i++){
						System.out.println("running3");
						int socialClass = getClassIndex(percen);
						cT[socialClass]++;
						writeRow(
								wb.getSheetAt(0),
								getCharacter(driver,race,socialClass),
								rowNum
						);
						String res = "Getting " + races[race] + " number " + i + " / " + num + "   :  " + cT[0] + "| "+cT[1]+"| "+cT[2]+"| "+cT[3]+"| "+cT[4]+"| "+cT[5]+"| "+cT[6];
						error.setText(res);		
						f.update(f.getGraphics());
						rowNum++;
						if(canceled)
							throw new IllegalArgumentException("Task Stopped");
					}
				else 
					throw new IllegalArgumentException("Error: Race Name Error");						
			}
			
			outputExcel(wb,"Success");
		}catch(NumberFormatException e) {
			error.setText("Error: Number Input Parsing Error");
		}catch(IndexOutOfBoundsException e) {
			error.setText("Error: Delim Input Parsing Error");
		}catch(IllegalArgumentException e){
			error.setText(e.getMessage());
			switch(e.getMessage()) {
				case "Task Stopped": 
					outputExcel(wb,"Stopped");
					break;
				case "Connection Dead": 
					outputExcel(wb,"ConnectionLost");
					break;
			}
		}catch(WebDriverException e) {
			driver.quit();
		    System.exit(0);
		}
		f.update(f.getGraphics());
		return new Integer(1);
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
	    	}
	    	if(System.currentTimeMillis() - timeout > 60000)
	    		break;
    	}
    	if(retry)
    		throw new IllegalArgumentException("Connection Dead");
    	rest();
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
    
	private static void rest(){
    	long targetTime = (long)(Math.random() * 200 + 200) + System.currentTimeMillis();
    	while(targetTime > System.currentTimeMillis()){}
    }
	
	private void outputExcel(SXSSFWorkbook wb, String extra) {
		int attempt = 0;
		boolean exit = false;
		while(!exit) {
	    	try {
	    		String fileName;
	    		if(attempt > 0)
	    			fileName = destPath + "\\ResultCharacterSet" + extra + attempt + ".xlsx";
	    		else
	    			fileName = destPath + "\\ResultCharacterSet" + extra + ".xlsx";
	        	FileOutputStream excelOutputStream = new FileOutputStream(fileName);
	        	exit = true;
				wb.write(excelOutputStream);
				exit = true;
				excelOutputStream.close();			
				error.setText("Success: at " + fileName);
			} catch (IOException e) {
				attempt++;
			}
		}
	}	    		
}
