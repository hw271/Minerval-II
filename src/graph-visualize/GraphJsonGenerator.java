package memex;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Stack;
//AU5QITY2B6q1kX2vxbqH
import org.json.simple.JSONValue;

import com.google.gson.Gson;


public class GraphJsonGenerator {
	//input:[[{XD34g-s245(part#)},{Xilinx(manufacturer)}],[{"value":"xilinx","product":"part#"},{"value":"s","seller":"有限公司"}]]
	
	ArrayList<Company> company = new ArrayList<Company>();
	ArrayList<Product> product = new ArrayList<Product>();
	ArrayList<Link> link = new ArrayList<Link>();
	ArrayList<Buyer> buyer = new ArrayList<Buyer>();
	private static HashMap<String, String> map_company = new HashMap<String,String>();
	private static HashMap<String, String> map_product = new HashMap<String,String>();
	{	try {
			generateMapForCompany();
			generateMapForProduct();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	private static String md5(String s) throws NoSuchAlgorithmException{
		String original = s;
		MessageDigest md = MessageDigest.getInstance("MD5");
		md.update(original.getBytes());
		byte[] digest = md.digest();
		StringBuffer sb = new StringBuffer();
		for (byte b : digest) {
			sb.append(String.format("%02x", b & 0xff));
		}
		return sb.toString();
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
/*	
	private String initialize() throws IOException{
		File file = new File("./final.json");
		FileInputStream fis = new FileInputStream(file); 
		byte[] data = new byte[(int) file.length()]; 
		fis.read(data); 
		fis.close();
		String initialJson = new String(data);
		return initialJson;
	}
	
	private static String combine(String initialText, String jsonText){
		String mainI = initialText.replace("{\"companies\":\n[\n", "").replace("]\n}", "");
		String mainJ = jsonText.replace("{\"companies\":\n[\n{", "").replace("]\n}", "");
		System.out.println(mainI);
		System.out.println(mainJ);
		
		String[] partI = mainI.split("\\],\"products\":\\[");
		String companiesI = partI[0];
		String[] partJ = mainJ.split("\\],\"products\":\\[");
		String companiesJ = partJ[0];
		
		System.out.println(companiesI);
		System.out.println(companiesJ);
		
		System.out.println(partI[1]);
		System.out.println(partJ[1]);
		
		//],"link":[
		String[] partII = partI[1].split("\\],\"links\":\\[");
		String productI = partII[0];
		System.out.println("\n"+productI);
		String linkI = partII[1];
		
		String[] partJJ = partJ[1].split("\\],\"links\":\\[");
		String productJ = partJ[0];
		String linkJ = partJJ[1];
		
		

		
		System.out.println(linkI);
		System.out.println(linkJ);
		System.out.println(productI);
		System.out.println(productJ);
		
		return jsonText;
		
	}
*/	
	public static void main(String[] args) throws IOException, NoSuchAlgorithmException{
//		String s = args[0];
		
		BufferedReader br = new BufferedReader(new FileReader(new File(args[0])));
		String s = br.readLine();
		String[] temp = s.split("\\],\\[");
		String userTag = temp[0].substring(2);
		String docuTag = temp[1].substring(1, temp[1].length()-3);
		
		//--------------userTag---------------
		GraphJsonGenerator generator = new GraphJsonGenerator();	
//		generator.initialize();
		
//		int id=200;
		GraphJsonGenerator.Company company = null;
		GraphJsonGenerator.Product product = null;
		GraphJsonGenerator.Buyer buyer = generator.new Buyer();
		GraphJsonGenerator.Link link = generator.new Link();		
		
		Map<String, Boolean> map_product = new HashMap<String, Boolean>();
		Map<String, Boolean> map_company = new HashMap<String, Boolean>();
		
		
		
		Stack<Element> stack  = new Stack<Element>();
	
		//--------------docuTag---------------
		//{"field":"Product","key":"Part#",value":"CY7C1470V33-167AXI"},{"field":"Product","key":"Manufacturer",value":"CYPRESS"},{"field":"Product","key":"DeviceType",value":"SRAM Chip Sync Quad"},{"field":"Product","key":"part#",value":"IMX225LQR"},{"field":"Product","key":"Manufacturer",value":"SONY"},{"field":"Product","key":"DeviceType",value":"CMOS"},{"field":"Product","key":"part#",value":"IMX225LQR"},{"field":"Product","key":"Manufacturer",value":"SONY"},{"field":"Product","key":"DeviceType",value":"CMOS"},{"field":"Product","key":"part#",value":"XC6SLX25-2CSG324C"},{"field":"Product","key":"Manufacturer",value":"XILINX"},{"field":"Product","key":"DeviceType",value":"FBGA"},{"field":"Product","key":"part#",value":"XC6SLX25-2CSG324C"},{"field":"Product","key":"Manufacturer",value":"XILINX"},{"field":"Product","key":"DeviceType",value":"FBGA"},{"field":"Seller","key":"Name",value":"杭州欧驰电子科技有限公司"},{"field":"Seller","key":"address",value":"中国 浙江 杭州 拱墅区登云路 518 号 3 幢 911 室"},{"field":"Seller","key":"employee",value":"王波"},{"field":"Seller","key":"telephone",value":"86 0571 85777975-8002"},{"field":"Seller","key":"employee",value":"王小姐"}

		stack  = new Stack<Element>();
		
		//System.out.println("--------------docuTag---------------");
		//System.out.println(docuTag);
		String[] docTexts = docuTag.split("\\},\\{");
		for(String docText:docTexts){
			////System.out.println(docText);
			String[] parts = docText.split(",");
			String parseText;
			parseText = parts[0].split(":")[1];
			String domain = parseText.substring(1, parseText.length()-1);
			parseText = parts[1].split(":")[1];
			String key = parseText.substring(1, parseText.length()-1);
			parseText = parts[2].split(":")[1];
			String value = parseText.substring(1, parseText.length()-1);
			////System.out.println(domain+":"+key+":"+value);
			
			if(domain.toLowerCase().equals("product")){
				if(key.toLowerCase().equals("part#")){
					if(!map_product.containsKey(value)){
						////System.out.println("******new product******");
						if(product!=null)generator.product.add(product);
						product = generator.new Product();
						product.partNumber = value;
						product.id = md5(value);
						map_product.put(value, true);
						if(!stack.isEmpty()){
							Element e = stack.peek();
							if(e.getClass().equals(Product.class)){
								stack.pop();
								stack.push(product);
							}else{
								link = generator.new Link();
								link.sourceid = ((Company)e).id;
								link.destid = String.valueOf(product.id);
								link.id = md5(link.sourceid+" "+link.destid);
								generator.link.add(link);
							}
						}else{
							stack.push(product);
						}
					}
				}else  if(product!=null){
					switch (key.toLowerCase()){
						case "manufacturer":
							product.manufactor = value;
							break;
						case "devicetype":
							product.deviceType = value;
							break;
						case "package":
							product.packageP = value;
							break;
					}
				}
				
			}else if(domain.toLowerCase().equals("seller")){
				if(key.toLowerCase().equals("name")){
					//System.out.println(value);
					if(!map_company.containsKey(value)){
						////System.out.println("******new company******");
						if(company!=null)generator.company.add(company);
						company = generator.new Company();
						company.id = md5(value);
						company.name.add(value);
						map_company.put(value,true);
						if(!stack.isEmpty()){
							Element e = stack.peek();
							if(e.getClass().equals(Company.class)){
								stack.pop();
								stack.push(product);
							}else{
								link = generator.new Link();
								link.sourceid = String.valueOf(((Product)e).id);
								link.destid = company.id;
								link.id = md5(link.sourceid+" "+link.destid);
								generator.link.add(link);
							}
						}else{
							stack.push(company);
						}
					}
				}else if(company!=null){
					switch (key.toLowerCase()){
						case "address":
							company.address.add(value);
							break;
						case "email":
							company.email.add(value);
							break;
						case "telephone":
							company.telephone.add(value);
							break;
						case "qq":
							company.qq.add(value);
							break;
						case "type":
							company.type = value;
							break;
						case "employ":
							company.employee.add(value);
					}
				}
			}else if(domain.toLowerCase().equals("buyer")){
				if(key.toLowerCase().equals("email")){
//					//System.out.println("******new buyer******");
					if(buyer!=null)generator.buyer.add(buyer);
					buyer.id = md5(value);
					buyer.email = value;
				}
			}
		}
		if(product!=null) generator.product.add(product);
		if(company!=null) generator.company.add(company);
		if(buyer!=null) generator.buyer.add(buyer);
		
		
		//System.out.println("--------------userTag---------------");
		////System.out.println(userTag);
		//[xxx(Part #)]
		String[] userTexts = userTag.split(",\\s*");
		for(String userText:userTexts){
			userText = userText.toLowerCase();
			//System.out.println(userText);
			String[] tempUserTag = userText.split("\\(");
			String value = tempUserTag[0];
			String key = tempUserTag[1].substring(0, tempUserTag[1].length()-1);
			//System.out.println(key+":"+value);
			String domain;
			if(generator.map_company.containsKey(key)){
				domain = generator.map_company.get(key);
			}else if(generator.map_product.containsKey(key)){
				domain = generator.map_product.get(key);
				if(key.equals("device type")){
					key = "DeviceType";
				}else if(key.equals("part #")){
					key = "part#";
				}
			}else{
				domain="unknown";
			}
			//System.out.println(domain+" "+key+" "+value);
			if(domain.toLowerCase().equals("product")){
				if(key.toLowerCase().equals("part#")){
					if(!map_product.containsKey(value)){
						////System.out.println("******new product******");
						if(product!=null)generator.product.add(product);
						product = generator.new Product();
						product.id = md5(value);
						product.partNumber = value;
						map_product.put(value, true);
						if(!stack.isEmpty()){
							Element e = stack.peek();
							if(e.getClass().equals(Product.class)){
								stack.pop();
								stack.push(product);
							}else{
								link = generator.new Link();
								link.sourceid = ((Company)e).id;
								link.destid = String.valueOf(product.id);
								generator.link.add(link);
							}
						}else{
							stack.push(product);
						}
					}
				}else if(product!=null){
					switch (key.toLowerCase()){
						case "manufacturer":
							product.manufactor = value;
							break;
						case "devicetype":
							product.deviceType = value;
							break;
						case "package":
							product.packageP = value;
							break;
					}
				}
				
			}else if(domain.toLowerCase().equals("seller")){
				if(key.toLowerCase().equals("name")){
					//System.out.println(value);
					if(!map_company.containsKey(value)){
						////System.out.println("******new company******");
						if(company!=null)generator.company.add(company);
						company = generator.new Company();
						company.id = md5(value);
						company.name.add(value);
						map_company.put(value,true);
						if(!stack.isEmpty()){
							Element e = stack.peek();
							if(e.getClass().equals(Company.class)){
								stack.pop();
								stack.push(product);
							}else{
								link = generator.new Link();
								link.id = md5(link.sourceid+" "+link.destid);
								link.sourceid = String.valueOf(((Product)e).id);
								link.destid = company.id;
								generator.link.add(link);
							}
						}else{
							stack.push(company);
						}
					}
				}else if(company!=null){
					switch (key.toLowerCase()){
						case "address":
							company.address.add(value);
							break;
						case "email":
							company.email.add(value);
							break;
						case "telephone":
							company.telephone.add(value);
							break;
						case "qq":
							company.qq.add(value);
							break;
						case "type":
							company.type = value;
							break;
						case "employ":
							company.employee.add(value);
					}
				}
			}else if(domain.toLowerCase().equals("buyer")){
				if(key.toLowerCase().equals("email")){
//					//System.out.println("******new buyer******");
					if(buyer!=null)generator.buyer.add(buyer);
					buyer.id = md5(value);
					buyer.email = value;
				}
			}
		}

		if(product!=null) generator.product.add(product);
		if(company!=null) generator.company.add(company);
		if(buyer!=null) generator.buyer.add(buyer);
		
	
		
		
		
		
		//System.out.println("--------------json---------------");
    	Gson gson = new Gson();
     
    	// convert java object to JSON format,
    	// and returned as JSON formatted string
    	String jsonText = gson.toJson(generator);
    	
    	jsonText = jsonText.replaceAll("company", "companies").replaceAll("product", "products").replaceAll("partNumber", "part #").replaceAll("deviceType", "device type").replaceAll("packageP", "package");
//    	String initialText = generator.initialize();
//    	jsonText = combine(jsonText, initialText);
//    	File f = new File("./graphJason.txt");
//    	FileWriter writer = new FileWriter(f);
//    	writer.write(jsonText);
//    	writer.close();
		//System.out.println(jsonText);
    	
		Writer out = new BufferedWriter(new OutputStreamWriter(
			    new FileOutputStream("./graphJason.txt"), "UTF-8"));
		try {
		    out.write(jsonText);
		} finally {
		    out.close();
		}
		
		
	}
	
//	public String toString(){
//		return "Company="+companyList+", product"+productList+", link"+linkList+", buyer"+buyerList;
//	}
	
	class Company implements Element{
		String id;
		LinkedList<String> name;
		LinkedList<String> address;
		String website;
		LinkedList<String> email;
		LinkedList<String> telephone;
		LinkedList<String> qq;
		LinkedList<String> employee;
		Bank bank;
		String type;

		public Company(){
			id="";
			name = new LinkedList<String>();
			address = new LinkedList<String>();
			website="";
			email = new LinkedList<String>();
			telephone = new LinkedList<String>();
			qq = new LinkedList<String>();
			employee = new LinkedList<String>();
			bank = new Bank();
			type="";
		}
		
		@Override
		public boolean equals(Object o){
			return (this.name.equals(o.toString()));
		}
		//return "DataObject [data1=" + data1 + ", data2=" + data2 + ", list="
		//+ list + "]";
//		public String toString(){
//			return "{id="+id+", name="+name+", address="+address+", website="+website+", email="+email+", telephone="
//					+telephone+", qq="+qq+", employee="+employee+", bank="+bank+", type="+type+"}";
//		}
	}
	
	class Bank{
		String bankname;
		String accountNumber;
		String bankAddress;
		
//		public String toString(){
//			return "{bankname="+bankname+", account #="+accountNumber+", bank address="+bankAddress+"}";
//		}
	}
	
	class Buyer implements Element{
		String id;
		String email;
		String qq;
		String telephone;
		
//		public String toString(){
//			return "{id="+id+", email="+email+", QQ="+qq+", telephone="+telephone+"}";
//		}
	}
	
	class Product implements Element{
		String id;
		String manufactor="";
		String partNumber="";
		String deviceType="";
		String packageP="";
		
		
		@Override
		public boolean equals(Object o){
			Product p =(Product)o;
			//System.out.println("arraylist element is:" + p.partNumber);
			//System.out.println("new element is:" + this.partNumber);
			return (this.partNumber.equals(p.partNumber));
		}
		public String toString(){
			return "part #="+partNumber;
			//return "{id="+id+", Manufacturer="+manufacturer+", part #="+partNumber+", device type="+deviceType+"package+"+packageP+"}";
		}
	}
	
	class Link{
		String id;
		String sourceid;
		String destid;
		
//		public String toString(){
//			return "{linkid="+id+", sourceid="+sourceid+", destid="+destid+"}";
//		}
	}
	
	interface Element{
		
	}
}
