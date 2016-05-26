package memex;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;


public class JsonGenerator {
	public static void main(String[] args) throws IOException{
		BufferedReader br = new BufferedReader(new FileReader(new File(args[0])));
		String line = br.readLine();
		String strTag="[";
		while(line!=null){
			//System.out.println(line);
			if(line.contains("<") && line.contains(">")){
				String[] tags = line.split("</tag>");
				for(String tag:tags){
					if(tag.contains("<tag name=")&&tag.contains(":")&&tag.contains(">")){
						int index1 = tag.indexOf("<");
						String tagLine=tag.substring(index1, tag.length());
						//System.out.println("tag:"+tagLine);
						//<tag name="seller:name">深圳市宇创芯科技有限公司</tag>
						
						int index_quote = tagLine.indexOf('\"');
						int index_maohao = tagLine.indexOf(':');
						int index_quote_last = tagLine.lastIndexOf('\"'); 
						String field = tagLine.substring(index_quote+1, index_maohao);
						String key = tagLine.substring(index_maohao+1, index_quote_last);
						String value = tagLine.substring(tagLine.indexOf('>')+1);
						if(key.equals("part#")||key.equals("part #")||key.equals("Part#"))
							key = "Part #";
						char start = key.charAt(0);	
						//System.out.println(start);
						start = Character.toUpperCase(start);
						//System.out.println(start);
						key = start + key.substring(1);
						String jsonLine = "{\"field\":\""+field+"\",\"key\":\""+key+"\",\"value\":\""+value+"\"}";
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
		
//		File file = new File("./tagResult.txt");
//		FileWriter writer = new FileWriter(file);
//		writer.write(strTag);
//		writer.close();
		
		Writer out = new BufferedWriter(new OutputStreamWriter(
			    new FileOutputStream("./tagResult.txt"), "UTF-8"));
		try {
		    out.write(strTag);
		} finally {
		    out.close();
		}
	}
}
