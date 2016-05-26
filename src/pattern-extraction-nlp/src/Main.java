/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


/**
 *
 * @author aa496
 */
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.process.TokenizerFactory;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.GrammaticalStructureFactory;
import edu.stanford.nlp.trees.PennTreebankLanguagePack;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreebankLanguagePack;
import edu.stanford.nlp.trees.TypedDependency;
import java.util.List;
import java.util.Map;
import java.io.*;
import java.awt.*;
import java.awt.font.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.util.ArrayList;
import java.util.Collection;
import javax.imageio.ImageIO;

import org.json.simple.JSONArray;
import org.json.simple.JSONValue;
import org.json.simple.parser.ContainerFactory;
import org.json.simple.parser.JSONParser;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

public class Main {
    
    private static TreebankLanguagePack tlp = new PennTreebankLanguagePack();
    private static GrammaticalStructureFactory gsf = tlp.grammaticalStructureFactory();
    
    public static void main(String[] args) throws Exception {

        //get the list
    	List<TaggedWord> taggedWords = getNounList(args[0], "pic");
    	System.out.println(taggedWords);
  
    	//===========================================================
    	System.out.println("=========encode jsonArray========");
		Gson gson = new Gson();
		JsonElement element = gson.toJsonTree(taggedWords, new TypeToken<List<TaggedWord>>(){}.getType());
		if (! element.isJsonArray()) {
		      throw new Exception();
		}
		JsonArray jsonArray = element.getAsJsonArray();
		System.out.println(jsonArray);
		String st = jsonArray.toString();
		
		List<TaggedWord> taggedWordsFromGson = gson.fromJson(st, new TypeToken<List<TaggedWord>>(){}.getType());
		System.out.println("=========decode jsonArray========");
		System.out.println(taggedWordsFromGson);
    	//=========================================================
    	  
		//================method 2=====================
		String st2 = gson.toJson(taggedWords);
		System.out.println("method2"+st2);
		//convert the json string back to object
 
		
		
		
    	//part 1: get the noun words list        
        List<String> nounList = new ArrayList<String>();
		for(int i=0;i<taggedWords.size();i++){
			TaggedWord taggedWord = taggedWords.get(i);
			String word="";
			if(taggedWord.tag().equals("CD")||taggedWord.tag().startsWith("NN")){
				word = taggedWord.value();
				while(true){
					if(i+1>=taggedWords.size()) break;
					i++;
					taggedWord = taggedWords.get(i);
					if(taggedWord.tag().equals("CD") || taggedWord.tag().startsWith("NN"))
						word+=" "+taggedWord.value();
					else
						break;
				}
				nounList.add(word);
		    }
		}

        //noun pairs
			List<String> nounPairList = new ArrayList<String>();
			//System.out.println("=============noun pair==============");
			for(int i=0;i<nounList.size();i++){
	            for(int j=i+1;j<nounList.size();j++){
                    nounPairList.add(nounList.get(i)+", "+nounList.get(j));
                }
            }
			
			for(int i=0;i<nounPairList.size();i++){
				System.out.println(nounPairList.get(i));	
			}


    }
    
    private static List<String> getBQuery(String AC, String paragraph) throws Exception{
    	List<String> BQuery = new ArrayList<String>();
    	//System.out.println("AC="+AC);
    	String[] pair = AC.split(",\\s");
    	String A = pair[0];
    	String C = pair[1];
    	String[] t1 = paragraph.split(A);
    	String[] t2 = t1[1].split(C);
    	String B = t2[0];
    	System.out.println(B);
    	
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
		int windowSize = B.split("\\s").length*2;
		BQuery.add("#uw"+windowSize*3+"("+B+")");
		BQuery.add("#uw"+windowSize*10+"("+B+")");
		BQuery.add(B);
		BQuery.add(importantTokenString);
		
		return BQuery;
    }
    
    
    private static void printHelp() throws Exception {
        //System.out.println("Usage: com.chaoticity.dependensee.Main <sentence> <image file>");
        //System.out.println("Usage: com.chaoticity.dependensee.Main -t <input file> <image file>");
    }
    
    private static Graph getGraph(Tree tree) throws Exception {
        ArrayList<TaggedWord> words = tree.taggedYield();
        GrammaticalStructure gs = gsf.newGrammaticalStructure(tree);
        Collection<TypedDependency> tdl = gs.typedDependencies();
        Graph g = new Graph(words);
        for (TypedDependency td : tdl) {
            g.addEdge(td.gov().index() - 1, td.dep().index() - 1, td.reln().toString());
        }
        try {
            g.setRoot(GrammaticalStructure.getRoots(tdl).iterator().next().gov().toString());
        } catch (Exception ex) {
            //System.err.println("Cannot find dependency graph root. Setting root to first");
            if (g.nodes.size() > 0) {
                g.setRoot(g.nodes.get(0).label);
            }
        }
        return g;
    }
    
    public static Graph getGraph(String sentence) throws Exception {
        LexicalizedParser lp = LexicalizedParser.loadModel("edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz");
        lp.setOptionFlags(new String[]{"-maxLength", "500", "-retainTmpSubcategories"});
        TokenizerFactory<CoreLabel> tokenizerFactory =
                PTBTokenizer.factory(new CoreLabelTokenFactory(), "");
        List<CoreLabel> wordList = tokenizerFactory.getTokenizer(new StringReader(sentence)).tokenize();
        Tree tree = lp.apply(wordList);
        GrammaticalStructure gs = gsf.newGrammaticalStructure(tree);
        Collection<TypedDependency> tdl = gs.typedDependencies();
        return getGraph(tree, tdl);
    }
    
    public static Graph getGraph(String sentence, LexicalizedParser lp) throws Exception {
        TokenizerFactory<CoreLabel> tokenizerFactory =
                PTBTokenizer.factory(new CoreLabelTokenFactory(), "");
        List<CoreLabel> wordList = tokenizerFactory.getTokenizer(new StringReader(sentence)).tokenize();
        Tree tree = lp.apply(wordList);
        GrammaticalStructure gs = gsf.newGrammaticalStructure(tree);
        Collection<TypedDependency> tdl = gs.typedDependencies();
        return getGraph(tree, tdl);
    }
    
    private static Graph getGraph(Tree tree, Collection<TypedDependency> tdl) throws Exception {
        ArrayList<TaggedWord> words = tree.taggedYield();
        GrammaticalStructure gs = gsf.newGrammaticalStructure(tree);
        Graph g = new Graph(words);
        for (TypedDependency td : tdl) {
            g.addEdge(td.gov().index() - 1, td.dep().index() - 1, td.reln().toString());
        }
        try {
            g.setRoot(GrammaticalStructure.getRoots(tdl).iterator().next().gov().toString());
        } catch (Exception ex) {
            //System.err.println("Cannot find dependency graph root. Setting root to first");
            if (g.nodes.size() > 0) {
                g.setRoot(g.nodes.get(0).label);
            }
        }
        
        return g;
    }

    private static int getNextHeight(Graph graph, Edge n) {
        int height = 3;
        boolean isFree = false;
        while (!isFree) {
            boolean overlapped = false;
            for (Edge e : graph.edges) {
                if (!e.visible || n == e) {
                    continue;
                }
                int eFirst = e.sourceIndex < e.targetIndex ? e.sourceIndex : e.targetIndex;
                int eSecond = e.sourceIndex < e.targetIndex ? e.targetIndex : e.sourceIndex;
                int nFirst = n.sourceIndex < n.targetIndex ? n.sourceIndex : n.targetIndex;
                int nSecond = n.sourceIndex < n.targetIndex ? n.targetIndex : n.sourceIndex;
                if (e.height == height
                        && ((nFirst > eFirst && nFirst < eSecond)
                        || (nSecond > eFirst && nSecond < eSecond)
                        || (eSecond > nFirst && eSecond < nSecond)
                        || (eSecond > nFirst && eSecond < nSecond)
                        || (n.targetIndex == eFirst)
                        || (n.targetIndex == eSecond))) {
                    overlapped = true;
                    //System.out.println("overlap = "+ n +" and " + e + " at height " + height);
                }
            }
            if (!overlapped) {
                isFree = true;
            } else {
                height++;
            }
            
        }
        return height;
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
    
    public static void writeImage(String sentence, String outFile, LexicalizedParser lp) throws Exception {
        
        Tree parse;
        try {
            TokenizerFactory<CoreLabel> tokenizerFactory =
                PTBTokenizer.factory(new CoreLabelTokenFactory(), "");
        List<CoreLabel> wordList = tokenizerFactory.getTokenizer(new StringReader(sentence)).tokenize();
            parse = lp.apply(wordList);            
        } catch (Exception e) {
            throw e;
        }
        writeImage(parse, outFile);
        
    }
    
    public static void writeImage(Tree tree, String outFile) throws Exception {
        writeImage(tree, outFile, 1);
    }
    
    public static void writeImage(Tree tree, Collection<TypedDependency> tdl, String outFile) throws Exception {
        Graph g = getGraph(tree, tdl);
        BufferedImage image = createTextImage(g, 1);
        ImageIO.write(image, "png", new File(outFile));
    }
    
    public static void writeImage(Tree tree, Collection<TypedDependency> tdl, String outFile, int scale) throws Exception {
        Graph g = getGraph(tree, tdl);
        BufferedImage image = createTextImage(g, scale);
        ImageIO.write(image, "png", new File(outFile));
    }
    
    public static void writeImage(Tree tree, String outFile, int scale) throws Exception {
        Graph g = getGraph(tree);
        BufferedImage image = createTextImage(g, scale);
        ImageIO.write(image, "png", new File(outFile));
    }
    
    private static BufferedImage createTextImage(Graph graph, int scale) throws Exception {
        
        Font wordFont = new Font("Arial", Font.PLAIN, 12 * scale);
        FontRenderContext frc = new FontRenderContext(null, true, false);
        
        int spaceHeight = 20 * scale;
        int spaceWidth = 20 * scale;
        double totalWidth = spaceWidth;

        // calculate word positions
        for (Integer i : graph.nodes.keySet()) {
            Node node = graph.nodes.get(i);
            TextLayout layout = new TextLayout(node.toString(), wordFont, frc);
            Rectangle2D bounds = layout.getBounds();
            node.position.setRect(totalWidth, 0, bounds.getWidth(), bounds.getHeight());
            totalWidth += node.position.getWidth() + spaceWidth;
        }
        int imageWidth = (int) Math.ceil(totalWidth);
        int imageHeight = spaceHeight * (6 * scale + graph.nodes.size());
        int baseline = imageHeight - 30 * scale;

        // create image
        BufferedImage image = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setBackground(Color.white);
        g.clearRect(0, 0, imageWidth, imageHeight);
        g.setColor(Color.black);
        g.setFont(wordFont);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_OFF);


        // draw words
        for (Integer i : graph.nodes.keySet()) {
            Node node = graph.nodes.get(i);
            node.position.setRect(node.position.getX(), baseline - spaceHeight, node.position.getWidth(), node.position.getHeight());
            g.drawString(node.toString(), (int) node.position.getX(), (int) node.position.getY());
        }
        
        Font posFont = new Font("Arial", Font.PLAIN, 8 * scale);
        g.setColor(Color.darkGray);
        g.setFont(posFont);
        for (Integer i : graph.nodes.keySet()) {
            Node node = graph.nodes.get(i);
            node.position.setRect(node.position.getX(), baseline - 10 * scale, node.position.getWidth(), node.position.getHeight());
            g.drawString(node.pos, (int) node.position.getX(), (int) node.position.getY());
        }
        g.setColor(Color.black);

        // draw lines
        int lineDistance = 5 * scale;
        int arrowBase = 2 * scale;
        int maxHeight = 0;
        for (Integer i : graph.nodes.keySet()) {
            Node node = graph.nodes.get(i);
            int spacer = (int) node.position.getWidth() / 2 - (node.outEdges.size() / 2 * lineDistance);
            for (Edge e : node.outEdges) {
                int height = getNextHeight(graph, e);
                if (height > maxHeight) {
                    maxHeight = height;
                }
                e.height = height;
                int targetSpacer = (int) e.target.position.getWidth() / 2 - ((e.target.outEdges.size() + 2) / 2 * lineDistance);
                // horizontal line
                g.drawLine(
                        (int) e.source.position.getX() + spacer,
                        baseline - (height * spaceHeight),
                        (int) e.target.position.getX() + targetSpacer,
                        baseline - (height * spaceHeight));

                // source vertical line
                g.drawLine(
                        (int) e.source.position.getX() + spacer,
                        baseline - (height * spaceHeight),
                        (int) e.source.position.getX() + spacer,
                        baseline - spaceHeight * 2);


                // target vertical line
                g.drawLine(
                        (int) e.target.position.getX() + targetSpacer,
                        baseline - (height * spaceHeight),
                        (int) e.target.position.getX() + targetSpacer,
                        baseline - spaceHeight * 2);

                // target arrowhead
                g.drawLine(
                        (int) e.target.position.getX() - arrowBase + targetSpacer,
                        baseline - spaceHeight * 2 - 4 * scale,
                        (int) e.target.position.getX() + targetSpacer,
                        baseline - spaceHeight * 2);
                g.drawLine(
                        (int) e.target.position.getX() + arrowBase + targetSpacer,
                        baseline - spaceHeight * 2 - 4 * scale,
                        (int) e.target.position.getX() + targetSpacer,
                        baseline - spaceHeight * 2);
                e.visible = true;
                spacer += lineDistance;
            }
            
        }

        //draw relation labels

        Font relFont = new Font("Arial", Font.PLAIN, 10 * scale);
        g.setColor(Color.blue);
        g.setFont(relFont);
        
        for (Integer i : graph.nodes.keySet()) {
            Node node = graph.nodes.get(i);
            int spacer = (int) node.position.getWidth() / 2 - (node.outEdges.size() / 2 * lineDistance);
            for (Edge e : node.outEdges) {
                int targetSpacer = (int) e.target.position.getWidth() / 2 - ((e.target.outEdges.size() + 2) / 2 * lineDistance);
                int x = (int) (e.source.position.getX() < e.target.position.getX()
                        ? e.source.position.getX() + spacer
                        : e.target.position.getX() + targetSpacer);
                TextLayout layout = new TextLayout(e.label, relFont, frc);
                Rectangle2D bounds = layout.getBounds();
                int clearWidth = (int) Math.ceil(bounds.getWidth());
                int clearHeight = (int) Math.ceil(bounds.getHeight()) + 2 * scale;
                g.clearRect(x, baseline - (e.height * spaceHeight) - clearHeight - 2 * scale,
                        clearWidth, clearHeight);
                g.drawString(e.label, x, baseline - (e.height * spaceHeight) - 3 * scale);
                
                spacer += lineDistance;
            }
        }
        
        g.dispose();
        int ystart = imageHeight - spaceHeight * (maxHeight + 3 * scale);
        return image.getSubimage(0, ystart, imageWidth, imageHeight - ystart);
    }
    
    public static void writeFromTextFile(String infile, String outfile) throws Exception {
        Graph g = new Graph();
        BufferedReader input = new BufferedReader(new FileReader(infile));
        String line = null;
        while ((line = input.readLine()) != null) {
            if ("".equals(line)) {
                continue;
            }
            int relEnd = line.indexOf("(");
            int secondWordStart = line.indexOf(", ", relEnd + 1);
            String rel = line.substring(0, relEnd);
            String gov = line.substring(relEnd + 1, secondWordStart);
            String dep = line.substring(secondWordStart + 2, line.length() - 1);
            Node govNode = g.addNode(gov, "");
            Node depNode = g.addNode(dep, "");
            g.addEdge(govNode, depNode, rel);
        }
        
        BufferedImage image = createTextImage(g, 1);
        ImageIO.write(image, "png", new File(outfile));
    }
}
