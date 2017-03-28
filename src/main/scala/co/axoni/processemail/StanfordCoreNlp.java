package co.axoni.processemail;

import java.io.*;
import java.util.*;

import edu.stanford.nlp.io.*;
import edu.stanford.nlp.ling.*;
import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.trees.*;
import edu.stanford.nlp.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** This class demonstrates building and using a Stanford CoreNLP pipeline. */
public class StanfordCoreNlp {

    private final static Logger logger = LoggerFactory.getLogger(StanfordCoreNlp.class);

    private static StanfordCoreNlp instance = null;
    private StanfordCoreNLP pipeline = null;

    private StanfordCoreNlp() {
        pipeline = new StanfordCoreNLP();
    }

    public static StanfordCoreNlp getInstance() {
        if (instance == null) {
            instance = new StanfordCoreNlp();
        }
        return instance;
    }

    public void setRefTime(String date) {

    }

    public List<String> getNumbers(String input) {
        Annotation annotation = new Annotation(input);
        pipeline.annotate(annotation);
        List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);
        List<String> result = new ArrayList<>();
        if (sentences != null && ! sentences.isEmpty()) {
            for (int i=0; i<sentences.size(); i++) {
                CoreMap sentence = sentences.get(i);
                List<CoreLabel> tokens = sentence.get(CoreAnnotations.TokensAnnotation.class);
                int tokenCount = 0;
                for (CoreLabel token : tokens) {
                    tokenCount++;
                    //logger.debug("Token:" + token.word());
                    if(token.get(CoreAnnotations.PartOfSpeechAnnotation.class).equals("JJ") || token.get(CoreAnnotations.PartOfSpeechAnnotation.class).equals("CD")) {
                        logger.debug("Found JJ/CD!");
                        result.add(token.word().replaceAll("[a-zA-Z]*",""));
                    }
                }
            }
        }
        return result;
    }

    static List<CoreLabel> tokens;

    public List<MeetingTime> getTimes(String input, String refTime, boolean emilyTrigger) {
        Annotation annotation = new Annotation(input);
        logger.debug("refTime: " + refTime);
        annotation.set(CoreAnnotations.DocDateAnnotation.class, refTime);
        pipeline.annotate(annotation);
        List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);
        List<MeetingTime> result = new ArrayList<>();
        if (sentences != null && ! sentences.isEmpty()) {
            for (int i=0; i<sentences.size(); i++) {
                CoreMap sentence = sentences.get(i);
                if (emilyTrigger == true && (sentence.toString().toLowerCase().contains("emily") == false)) {
                    continue;
                }
                tokens = sentence.get(CoreAnnotations.TokensAnnotation.class);
                int tokenCount = 0;
                for (CoreMap token : tokens) {
                    tokenCount++;
                    if(token.get(CoreAnnotations.NamedEntityTagAnnotation.class).equals("TIME")) {
                        logger.debug("TIME -->" + token.get(CoreAnnotations.NormalizedNamedEntityTagAnnotation.class));
                        result.add(new MeetingTime(token.get(CoreAnnotations.NormalizedNamedEntityTagAnnotation.class).toString(),
                                "TIME",
                                token.toString(),
                                tokenCount,
                                i));
                    }

                    if(token.get(CoreAnnotations.NamedEntityTagAnnotation.class).equals("DATE")) {
                        logger.debug("OFFSET -->" + token.get(CoreAnnotations.NormalizedNamedEntityTagAnnotation.class));
                        result.add(new MeetingTime(token.get(CoreAnnotations.NormalizedNamedEntityTagAnnotation.class).toString(),
                                "OFFSET",
                                token.toString(),
                                tokenCount,
                                i));
                    }

                    if(token.get(CoreAnnotations.NamedEntityTagAnnotation.class).equals("DURATION")) {
                        logger.debug("DURATION -->" + token.get(CoreAnnotations.NormalizedNamedEntityTagAnnotation.class));
                        result.add(new MeetingTime(token.get(CoreAnnotations.NormalizedNamedEntityTagAnnotation.class).toString(),
                                "DURATION",
                                token.toString(),
                                tokenCount,
                                i));
                    }

                }
            }
        }
        return result;
    }


    public Set<String> parseTimeAndLoc(String input, String refTime, boolean emilyTrigger) {
        logger.debug("parseTimeAndLoc called");
        Annotation annotation = new Annotation(input);
        annotation.set(CoreAnnotations.DocDateAnnotation.class, refTime);
        pipeline.annotate(annotation);
        List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);
        Set<String> result = new HashSet<String>();
        if (sentences != null && ! sentences.isEmpty()) {
            for (int i=0; i<sentences.size(); i++) {
                CoreMap sentence = sentences.get(i);
                if (emilyTrigger == true && (sentence.toString().toLowerCase().contains("emily") == false)) {
                    continue;
                }
                tokens = sentence.get(CoreAnnotations.TokensAnnotation.class);
                for (CoreMap token : tokens) {

                    if(token.get(CoreAnnotations.NamedEntityTagAnnotation.class).equals("TIME")) {
                        logger.debug("TIME -->" + token.get(CoreAnnotations.NormalizedNamedEntityTagAnnotation.class));
                        result.add(token.get(CoreAnnotations.NormalizedNamedEntityTagAnnotation.class));
                    }

                    if(token.get(CoreAnnotations.NamedEntityTagAnnotation.class).equals("DATE")) {
                        logger.debug("OFFSET -->" + token.get(CoreAnnotations.NormalizedNamedEntityTagAnnotation.class));
                        result.add(token.get(CoreAnnotations.NormalizedNamedEntityTagAnnotation.class));
                    }

                    if(token.get(CoreAnnotations.NamedEntityTagAnnotation.class).equals("DURATION")) {
                        logger.debug("DURATION -->" + token.get(CoreAnnotations.NormalizedNamedEntityTagAnnotation.class));
                        result.add(token.get(CoreAnnotations.NormalizedNamedEntityTagAnnotation.class));
                    }

                }
            }
        }
        return result;
    }

    public Set<String> parseTime(String input) {
        logger.debug("parseTime called");
        Annotation annotation = new Annotation(input);
        pipeline.annotate(annotation);
        List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);
        Set<String> result = new HashSet<String>();
        if (sentences != null && ! sentences.isEmpty()) {
            for (int i=0; i<sentences.size(); i++) {
                CoreMap sentence = sentences.get(i);
                tokens = sentence.get(CoreAnnotations.TokensAnnotation.class);
                for (CoreMap token : tokens) {

                    if(token.get(CoreAnnotations.NamedEntityTagAnnotation.class).equals("TIME")) {
                        logger.debug("TIME -->" + token.get(CoreAnnotations.NormalizedNamedEntityTagAnnotation.class));
                        result.add(token.get(CoreAnnotations.NormalizedNamedEntityTagAnnotation.class));
                    }

                    if(token.get(CoreAnnotations.NamedEntityTagAnnotation.class).equals("DATE")) {
                        logger.debug("OFFSET -->" + token.get(CoreAnnotations.NormalizedNamedEntityTagAnnotation.class));
                        result.add(token.get(CoreAnnotations.NormalizedNamedEntityTagAnnotation.class));
                    }

                    if(token.get(CoreAnnotations.NamedEntityTagAnnotation.class).equals("DURATION")) {
                        logger.debug("DURATION -->" + token.get(CoreAnnotations.NormalizedNamedEntityTagAnnotation.class));
                        result.add(token.get(CoreAnnotations.NormalizedNamedEntityTagAnnotation.class));
                    }

                }
            }
        }
        return result;

    }

    public Set<String> parseRequest(String input, String refTime) {
        logger.debug("parseRequest called");
        logger.debug("refTime: "+refTime);
        Annotation annotation = new Annotation(input);
        annotation.set(CoreAnnotations.DocDateAnnotation.class, refTime);
        logger.debug("SUTime docDate: " + annotation.get(CoreAnnotations.DocDateAnnotation.class));
        pipeline.annotate(annotation);
        List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);
        Set<String> result = new HashSet<String>();
        if (sentences != null && ! sentences.isEmpty()) {
            for (int i=0; i<sentences.size(); i++) {
                CoreMap sentence = sentences.get(i);
                tokens = sentence.get(CoreAnnotations.TokensAnnotation.class);
                for (CoreMap token : tokens) {

                    if(token.get(CoreAnnotations.NamedEntityTagAnnotation.class).equals("PERSON")) {
                        logger.debug("PERSON -->" + token.get(CoreAnnotations.ValueAnnotation.class));
                        result.add("PERSON-->"+token.get(CoreAnnotations.ValueAnnotation.class));
                    }

                    if(token.get(CoreAnnotations.NamedEntityTagAnnotation.class).equals("TIME")) {
                        logger.debug("TIME -->" + token.get(CoreAnnotations.NormalizedNamedEntityTagAnnotation.class));
                        result.add("TIME-->"+token.get(CoreAnnotations.NormalizedNamedEntityTagAnnotation.class));
                    }

                    if(token.get(CoreAnnotations.NamedEntityTagAnnotation.class).equals("DATE")) {
                        logger.debug("OFFSET -->" + token.get(CoreAnnotations.NormalizedNamedEntityTagAnnotation.class));
                        result.add("OFFSET-->"+token.get(CoreAnnotations.NormalizedNamedEntityTagAnnotation.class));
                    }

                    if(token.get(CoreAnnotations.NamedEntityTagAnnotation.class).equals("DURATION")) {
                        logger.debug("DURATION -->" + token.get(CoreAnnotations.NormalizedNamedEntityTagAnnotation.class));
                        result.add("DURATION-->"+token.get(CoreAnnotations.NormalizedNamedEntityTagAnnotation.class));
                    }

                }

                Tree tree = sentence.get(TreeCoreAnnotations.TreeAnnotation.class);

                findWhere(tree, tree, 0, false, false, false, true, true, new PrintWriter(System.out));
            }
        }
        return result;

    }

    /** Usage: java -cp "*" StanfordCoreNlpDemo [inputFile [outputTextFile [outputXmlFile]]] */
    public void parseRequest(String[] args) throws IOException {
        // set up optional output files
        PrintWriter out;
        if (args.length > 1) {
            out = new PrintWriter(args[1]);
        } else {
            out = new PrintWriter(System.out);
        }
        PrintWriter xmlOut = null;
        if (args.length > 2) {
            xmlOut = new PrintWriter(args[2]);
        }

        // Create a CoreNLP pipeline. This line just builds the default pipeline.
        // In comments we show how you can build a particular pipeline
        // Properties props = new Properties();
        // props.put("annotators", "tokenize, ssplit, pos, lemma, ner, depparse");
        // props.put("ner.model", "edu/stanford/nlp/models/ner/english.all.3class.distsim.crf.ser.gz");
        // props.put("ner.applyNumericClassifiers", "false");
        // StanfordCoreNLP pipeline = new StanfordCoreNLP(props);


        // Initialize an Annotation with some text to be annotated. The text is the argument to the constructor.
        Annotation annotation;
        if (args.length > 0) {
            annotation = new Annotation(IOUtils.slurpFileNoExceptions(args[0]));
        } else {
            annotation = new Annotation("Kosgi Santosh sent an email to Stanford University. He didn't get a reply.");
        }

        // run all the selected Annotators on this text
        pipeline.annotate(annotation);
/*
    // print the results to file(s)
    pipeline.prettyPrint(annotation, out);
    if (xmlOut != null) {
      pipeline.xmlPrint(annotation, xmlOut);
    }

    // Access the Annotation in code
    // The toString() method on an Annotation just prints the text of the Annotation
    // But you can see what is in it with other methods like toShorterString()
     logger.debug();
     logger.debug("The top level annotation");
     logger.debug(annotation.toShorterString());
*/

  /*
    Scanner s = new Scanner(new File(args[0]));
    ArrayList<String> list = new ArrayList<String>();
    while (s.hasNextLine()){
        list.add(s.nextLine());
    }
    s.close();
  */

        // An Annotation is a Map and you can get and use the various analyses individually.
        // For instance, this gets the parse tree of the first sentence in the text.
        List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);
        if (sentences != null && ! sentences.isEmpty()) {
            for (int i=0; i<sentences.size(); i++) {
                CoreMap sentence = sentences.get(i);

                //    logger.debug();
                //    logger.debug("Sentence " + i + " :" + list.get(i));
	  /*
	      logger.debug(sentence.toShorterString());
	      logger.debug();
	  */
                //logger.debug("Sentence tokens are:");
                tokens = sentence.get(CoreAnnotations.TokensAnnotation.class);
                for (CoreMap token : tokens) {

                    //logger.debug(token.toShorterString());

                    if(token.get(CoreAnnotations.NamedEntityTagAnnotation.class).equals("PERSON")) {
                        logger.debug("PERSON -->" + token.get(CoreAnnotations.ValueAnnotation.class));
                    }

                    if(token.get(CoreAnnotations.NamedEntityTagAnnotation.class).equals("TIME")) {
                        logger.debug("TIME -->" + token.get(CoreAnnotations.NormalizedNamedEntityTagAnnotation.class));
                    }

                    if(token.get(CoreAnnotations.NamedEntityTagAnnotation.class).equals("DATE")) {
                        logger.debug("OFFSET -->" + token.get(CoreAnnotations.NormalizedNamedEntityTagAnnotation.class));
                    }

                    if(token.get(CoreAnnotations.NamedEntityTagAnnotation.class).equals("DURATION")) {
                        logger.debug("DURATION -->" + token.get(CoreAnnotations.NormalizedNamedEntityTagAnnotation.class));
                    }

                }

                Tree tree = sentence.get(TreeCoreAnnotations.TreeAnnotation.class);
                //logger.debug();
                //logger.debug("Sentence parse tree is:");
                //tree.pennPrint(out);
                //logger.debug();

                //logger.debug("** Identifying Where **");
                findWhere(tree, tree, 0, false, false, false, true, true, out);

	  /*
	      logger.debug("Sentence basic dependencies are:");
	      logger.debug(sentence.get(SemanticGraphCoreAnnotations.BasicDependenciesAnnotation.class).toString(SemanticGraph.OutputFormat.LIST));
	      logger.debug("Sentence collapsed, CC-processed dependencies are:");
	      SemanticGraph graph = sentence.get(SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation.class);
	      logger.debug(graph.toString(SemanticGraph.OutputFormat.LIST));

	      // Access coreference. In the coreference link graph,
	      // each chain stores a set of mentions that co-refer with each other,
	      // along with a method for getting the most representative mention.
	      // Both sentence and token offsets start at 1!
	      logger.debug("Coreference information");
	      Map<Integer, CorefChain> corefChains =
	          annotation.get(CorefCoreAnnotations.CorefChainAnnotation.class);
	      if (corefChains == null) { return; }
	      for (Map.Entry<Integer,CorefChain> entry: corefChains.entrySet()) {
	        logger.debug("Chain " + entry.getKey() + " ");
	        for (CorefChain.CorefMention m : entry.getValue().getMentionsInTextualOrder()) {
	          // We need to subtract one since the indices count from 1 but the Lists start from 0
	          List<CoreLabel> tokens = sentences.get(m.sentNum - 1).get(CoreAnnotations.TokensAnnotation.class);
	          // We subtract two for end: one for 0-based indexing, and one because we want last token of mention not one following.
	          logger.debug("  " + m + ", i.e., 0-based character offsets [" + tokens.get(m.startIndex - 1).beginPosition() +
	                  ", " + tokens.get(m.endIndex - 2).endPosition() + ")");
	        }
	      }
	   */
            }
        }
        IOUtils.closeIgnoringExceptions(out);
        IOUtils.closeIgnoringExceptions(xmlOut);
    }

    private static String extractWhere(Tree tree, PrintWriter pw, String whereStr) {
        if (tree.isLeaf()) {
            for (CoreMap token : tokens) {
                //pw.logger.debug(token.toShorterString());
                //pw.print("token value: " + token.get(CoreAnnotations.ValueAnnotation.class));
                //pw.logger.debug(" tree value: " + tree.value());
                if (token.get(CoreAnnotations.ValueAnnotation.class).equals(tree.value())) {
                    // pw.logger.debug("Found value match!");
                    if (token.get(CoreAnnotations.NamedEntityTagAnnotation.class).equals("TIME") || token.get(CoreAnnotations.NamedEntityTagAnnotation.class).equals("DATE")) {
                        return "$*ZENADISCARD*$";
                    }
                }
            }
            //pw.logger.debug("Returning value: " + tree.value());
            return tree.value();
        }
        if (tree.value().equals("NP") || tree.isPreTerminal()) {
            for (Tree child : tree.children()) {
                //pw.logger.debug("Traversing child: " + child.value());
                String returnStr = extractWhere(child, pw, "");
                if (returnStr.equals("$*ZENADISCARD*$")) {
                    //pw.logger.debug("Found TIME|DATE tag");
                    whereStr = "$*ZENADISCARD*$";
                }
                else {
                    whereStr = whereStr.concat(returnStr + " ");
                    //pw.logger.debug("Child Concatenating returnStr: " + returnStr + " whereStr: " + whereStr);
                }
            }
            //pw.logger.debug("Returning string \"" + whereStr + "\" from extractWhere");
            return whereStr;
        }
        return "";
    }

    private static void findWhere(Tree parent, Tree tree, int indent, boolean parentLabelNull, boolean firstSibling, boolean leftSiblingPreTerminal, boolean topLevel, boolean onlyLabelValue, PrintWriter pw) {
        // the condition for staying on the same line in Penn Treebank
        boolean suppressIndent = (parentLabelNull || (firstSibling && tree.isPreTerminal()) || (leftSiblingPreTerminal && tree.isPreTerminal() && (tree.label() == null || !tree.label().value().startsWith("CC"))));

        if (tree.isLeaf() || tree.isPreTerminal()) {
            String terminalString = tree.toStringBuilder(new StringBuilder(), onlyLabelValue).toString();
            if (terminalString.equals("(IN at)") || terminalString.equals("(IN in)")) {
                //pw.logger.debug(terminalString);
                List<Tree> sibs = tree.siblings(parent);
                String whereStr = new String();
                for (Tree sibTree : sibs) {
                    //pw.logger.debug("New sibling:");
                    //sibTree.pennPrint(pw);
                    String whereStrSib = new String();
                    //pw.logger.debug("Before sibTree whereStr = " + whereStr);
                    whereStrSib = extractWhere(sibTree, pw,whereStrSib);
                    if (whereStrSib.contains("$*ZENADISCARD*$")) {
                        whereStrSib = "";
                    }
                    whereStr = whereStr.concat(whereStrSib + " ");
                    //pw.logger.debug("After sibTree whereStr = " + whereStr);
                }
                logger.debug("LOCATION --> " + whereStr);
            }
            return;
        }

        String nodeString;
        if (onlyLabelValue) {
            String value = tree.value();
            nodeString = (value == null) ? "" : value;
        } else {
            nodeString = tree.nodeString();
        }
        // pw.print(nodeString);
        // pw.flush();
        boolean parentIsNull = tree.label() == null || tree.label().value() == null;
        findWhereChildren(tree, tree.children(), indent + 1, parentIsNull, true, pw);
        // pw.print(")");
        // pw.flush();
    }

    private static void findWhereChildren(Tree parent, Tree[] trChildren, int indent, boolean parentLabelNull, boolean onlyLabelValue, PrintWriter pw) {
        boolean firstSibling = true;
        boolean leftSibIsPreTerm = true;  // counts as true at beginning
        for (Tree currentTree : trChildren) {
            findWhere(parent, currentTree, indent, parentLabelNull, firstSibling, leftSibIsPreTerm, false, onlyLabelValue, pw);
            leftSibIsPreTerm = currentTree.isPreTerminal();
            // CC is a special case for English, but leave it in so we can exactly match PTB3 tree formatting
            if (currentTree.value() != null && currentTree.value().startsWith("CC")) {
                leftSibIsPreTerm = false;
            }
            firstSibling = false;
        }
    }

}
