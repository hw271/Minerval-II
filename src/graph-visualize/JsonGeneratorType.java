package memex;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;

public class JsonGeneratorType {
	private static HashMap<String, String> map_company = new HashMap<String,String>();
	private static HashMap<String, String> map_product = new HashMap<String,String>();
	{	try {
			generateMapForCompany();
			generateMapForProduct();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	private static void generateMapForCompany() throws IOException{
		File f = new File("./tag/company.txt");
		BufferedReader br = new BufferedReader(new FileReader(f));
		String line = br.readLine();
		while(line!=null){
			map_company.put(line, "seller");
			line = br.readLine();
		}
	}
	
	private static void generateMapForProduct() throws IOException{
		File f = new File("./tag/product.txt");
		BufferedReader br = new BufferedReader(new FileReader(f));
		String line = br.readLine();
		while(line!=null){
			map_product.put(line, "product");
			line = br.readLine();
		}
	}
	
	private static String generateDK(String key){
		String domain;
		if(map_company.containsKey(key)){
			domain = map_company.get(key);
		}else if(map_product.containsKey(key)){
			domain = map_product.get(key);
			if(key.equals("device type")){
				key = "DeviceType";
			}else if(key.equals("part #")){
				key = "part#";
			}
		}else{
			domain="unknown";
		}
		return domain+":"+key;
	}
	
	public static void main(String[] args) throws IOException{
		JsonGeneratorType generator = new JsonGeneratorType();
		
		BufferedReader br = new BufferedReader(new FileReader(new File(args[0])));
		String string = br.readLine().toLowerCase();
		//System.out.println(string);
		
		
		String domain_key = generateDK(string);
		String[] temp = domain_key.split(":");
		String domain = temp[0];
		String key = temp[1];
		//System.out.println("<tag name=\""+domain_key+"\">");
		
		String line = br.readLine();
		String strTag="[";
		while(line!=null){
			//System.out.println(line);
			if(line.contains("</tag>")){
				String[] tags = line.split("</tag>");
				for(String tag:tags){
					String lowerTag="";
					if(tag.contains("tag")){ 
						lowerTag = tag.toLowerCase();
						//System.out.println(lowerTag);
					}
					
					if(lowerTag.contains("<tag name=\""+domain_key+"\">")){
						
						String value = tag.substring(tag.indexOf('>')+1);
						if(key.equals("part#")||key.equals("part #")||key.equals("Part#"))
							key = "Part #";
						char start = key.charAt(0);	
						start = Character.toUpperCase(start);
						key = start + key.substring(1);
						String jsonLine = "{\"field\":\""+domain+"\",\"key\":\""+key+"\",\"value\":\""+value+"\"}";
						if(strTag!="[") strTag+=",";
						strTag+=jsonLine;
						//[{"text":"深圳市宇创芯科技有限公","type":"company"},{"text":"深圳市宇创芯科技有限公2"}]
						//System.out.println(jsonLine);
						
					}
				}
			}
			line = br.readLine();
		}
		strTag+="]";
//		FileWriter writer = new FileWriter(new File("./typeTagResult.txt"));
//		writer.write(strTag);
//		writer.close();
		//System.out.println(strTag);
		
		Writer out = new BufferedWriter(new OutputStreamWriter(
			    new FileOutputStream("./typeTagResult.txt"), "UTF-8"));
		try {
		    out.write(strTag);
		} finally {
		    out.close();
		}
	}
}
