import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.process.TokenizerFactory;
import edu.stanford.nlp.trees.Tree;

public class Queries {
	public static void main(String[] args) throws Exception{
		//System.out.println("input:------------------");
		String nounPair = args[0];
		String paragraph = args[1];
		//System.out.println(nounPair);
		//System.out.println(paragraph);
		//[hello/VBG, guys/NNS, ,/,, I/PRP, am/VBP, an/DT, international/JJ, student/NN, of/IN, Georgetown/NNP, University/NNP, ./., I/PRP, like/IN, watching/NN, movies/NNS, ./., I/PRP, live/VBP, in/IN, park/NN, Georgetown/NNP, Apartment/NNP, ,/,, VA/NNP, ,/,, USA/NNP]
		
		//System.out.println("output:------------------");
		Queries query = new Queries();
		List<TTPair> BList = query.getB(nounPair, paragraph);
		//System.out.println(BList);
		
		List<String> queries = query.getBQueries(BList);
		for(int i=0; i<queries.size();i++){
			System.out.println(queries.get(i));
		}
		/*
		List<String> BQueries = getBQuery(args[0], args[1]);
		if(BQueries!=null){
			System.out.println(BQueries);
		}
		*/
	}
	
	private List<String> getBQueries(List<TTPair> BList){
		/*#weight( 0.75 #combine ( hubble telescope achievements )
				 0.25 #combine ( universe system mission search galaxies ) )
		
		*#uw50( query )
		*query
		*/
		List<String> queries = new ArrayList<String>();
		
		String importantWord = "";
		String otherWord = "";
		String allWord = "";
		for(int i=0; i<BList.size();i++){
			TTPair taggedWord = BList.get(i);
			if(taggedWord.getTag().equals("CD")||taggedWord.getTag().startsWith("NN")||taggedWord.getTag().startsWith("JJ")){
				importantWord += taggedWord.getToken()+" ";
			}else{
				otherWord += taggedWord.getToken()+" ";
			}
			allWord += taggedWord.getToken()+" ";
		}
		String weightQuery = "#weight(0.75 #combine ("+importantWord+") 0.25 #combine ("+otherWord+"))";
		String uwQuery3 = "#uw"+BList.size()*3+"("+allWord+")";
		String uwQuery10 = "#uw"+BList.size()*10+"("+allWord+")";
		String NormalQuery = allWord;
		String keyWordQuery = importantWord;
		
		queries.add(uwQuery3);
		queries.add(uwQuery10);
		queries.add(weightQuery);
		queries.add(NormalQuery);
		queries.add(keyWordQuery);
		
		return queries;
	}
	
	private List<TTPair> getB(String nounPair, String paragraph){
		//noun pair
		String AC = nounPair;
    	String[] ACpair = AC.split(",\\s");
    	String A = ACpair[0];
    	String C = ACpair[1];
		
    	String[] Alist = A.split("\\s");
    	String[] Clist = C.split("\\s");
    	
    	//paragraph
		paragraph = paragraph.substring(1, paragraph.length()-1);
		String[] pairsInPara = paragraph.split(",\\s");
		List<TTPair> listOfTokensInB = new ArrayList<TTPair>(pairsInPara.length);
		
		boolean startsB = false;
		boolean endsB = false;
		for(int i=0;i<pairsInPara.length;i++){
			String[] temp = pairsInPara[i].split("/");
			String token = temp[0];
			//find stop point
			if(token.equals(Clist[0])){
				endsB = true;
				for(int j=1;j<Clist.length;j++){
					String token1 = pairsInPara[i+j].split("/")[0];
					if(Clist[j].equals(token1)){
						endsB = true;
					}else{
						endsB = false;
						break;
					}
				}
				if(endsB){
					break;
				}
			}
			
			//if not end, add the token to the list
			if(startsB){
				String tag = temp[1];
				TTPair ttpair = new TTPair(token,tag);
				listOfTokensInB.add(ttpair);
			}
			
			//find start point
			if(token.equals(Alist[0])){
				startsB = true;
				for(int j=1;j<Alist.length;j++){
					String token1 = pairsInPara[i+j].split("/")[0];
					if(token1.equals(Alist[j])){
						startsB = true;
					}else{
						startsB = false;
						break;
					}
				}
				if(startsB){
					i += Alist.length-1;
				}
			}
		}
		return listOfTokensInB;
	}
	
	class TTPair{
		private String token;
		private String tag;
		public TTPair(String token, String tag){
			this.token = token;
			this.tag = tag;
		}
		
		public String getToken(){ return this.token;}
		public String getTag(){return this.tag; }
		public String toString(){
			return token+"/"+tag;
		}
	}
	/*
    private static List<String> getBQuery(String paragraph,String AC) throws Exception{
    	List<String> BQuery = new ArrayList<String>();
    	//System.out.println("AC="+AC);
    	String[] pair = AC.split(",\\s");
    	String A = pair[0];
    	String C = pair[1];
    	String[] t1 = paragraph.split(A);
    	String[] t2 = t1[1].split(C);
    	String B = t2[0];
    	//System.out.println(B);
    	
    	List<TaggedWord> taggedWords = getNounList(B, "pic");
        List<String> importantTokenList = new ArrayList<String>();
		for(int i=0;i<taggedWords.size();i++){
			TaggedWord taggedWord = taggedWords.get(i);
			String word="";
			if(taggedWord.tag().equals("CD")||taggedWord.tag().startsWith("NN")||taggedWord.tag().startsWith("JJ")){
				word = taggedWord.value();
				importantTokenList.add(word);
		    }
		}
		String importantTokenString="";
		for(int i=0;i<importantTokenList.size();i++){
			importantTokenString+=importantTokenList.get(i);
		}
		int windowSize = B.split("\\s").length;
		BQuery.add("#uw"+windowSize*3+"("+B+")");
		BQuery.add("#uw"+windowSize*10+"("+B+")");
		BQuery.add(B);
		BQuery.add(importantTokenString);
		
		return BQuery;
    }
    
    public static List<TaggedWord> getNounList(String sentence, String outFile) throws Exception {
        return writeNounList(sentence, outFile, 1);
    }
    
    public static List<TaggedWord> writeNounList(String sentence, String outFile, int scale) throws Exception {
        
        LexicalizedParser lp = null;
        try {
            lp = LexicalizedParser.loadModel("edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz");
        } catch (Exception e) {
            //System.err.println("Could not load file englishPCFG.ser.gz. Try placing this file in the same directory as Dependencee.jar");
            return null;
        }
        
        lp.setOptionFlags(new String[]{"-maxLength", "500", "-retainTmpSubcategories"});
        TokenizerFactory<CoreLabel> tokenizerFactory =
                PTBTokenizer.factory(new CoreLabelTokenFactory(), "");
        List<CoreLabel> wordList = tokenizerFactory.getTokenizer(new StringReader(sentence)).tokenize();
        //this part: get parse the word: System.out.println(wordList);
        Tree tree = lp.apply(wordList);
        //System.out.println(tree);
        
        List<TaggedWord> taggedWords = tree.taggedYield();
        return taggedWords;
    }
*/
}
