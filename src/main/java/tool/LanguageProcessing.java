package tool;

import opennlp.tools.chunker.ChunkerME;
import opennlp.tools.chunker.ChunkerModel;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LanguageProcessing {
    private final Tokenizer tokenizer;
    private final POSTaggerME posTagger;
    private final ChunkerME chunker;

    public LanguageProcessing() throws IOException {
        TokenizerModel tokenModel = new TokenizerModel(getClass().getResourceAsStream("/tool/en-token.bin"));
        tokenizer = new TokenizerME(tokenModel);

        POSModel posModel = new POSModel(getClass().getResourceAsStream("/tool/en-pos-maxent.bin"));
        posTagger = new POSTaggerME(posModel);

        ChunkerModel chunkerModel = new ChunkerModel(getClass().getResourceAsStream("/tool/en-chunker.bin"));
        chunker = new ChunkerME(chunkerModel);
    }

    public List<String> extractNounPhrases(String sentence) throws IOException {
        String[] tokens = tokenizer.tokenize(sentence);
        String[] tags = posTagger.tag(tokens);
        String[] chunks = chunker.chunk(tokens, tags);

        // Extracting noun phrases
        List<String> nounPhrases = new ArrayList<>();
        StringBuilder nounPhrase = new StringBuilder();
        for (int i = 0; i < chunks.length; i++) {
            if (chunks[i].startsWith("B-NP")) {
                nounPhrase.append(tokens[i]).append(' ');
            } else if (chunks[i].startsWith("I-NP")) {
                nounPhrase.append(tokens[i]).append(' ');
            } else if (nounPhrase.length() > 0) {
                nounPhrases.add(nounPhrase.toString().trim());
                nounPhrase.setLength(0);
            }
        }
        // Add the last noun phrase if it exists
        if (nounPhrase.length() > 0) {
            nounPhrases.add(nounPhrase.toString().trim());
        }
        return nounPhrases;
    }
}
