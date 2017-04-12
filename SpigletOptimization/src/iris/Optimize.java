package iris;

import org.deri.iris.Configuration;
import org.deri.iris.KnowledgeBase;
import org.deri.iris.api.IKnowledgeBase;
import org.deri.iris.api.basics.IPredicate;
import org.deri.iris.api.basics.IQuery;
import org.deri.iris.api.basics.IRule;
import org.deri.iris.api.terms.IVariable;
import org.deri.iris.compiler.Parser;
import org.deri.iris.optimisations.magicsets.MagicSets;
import org.deri.iris.storage.IRelation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Optimize {

	public LinkedList<IRelation> relationsList = new LinkedList<IRelation>();
	
	
	public void createAnalyses(String[] args) throws Exception {
		final String projectDirectory;
		if (args == null || args.length < 1) {
			projectDirectory = "./analyses/";
		}
		else {
			projectDirectory = args[0];
		}

		Parser parser = new Parser();

		Map<IPredicate, IRelation> factMap = new HashMap<>();
		/**
		 * The following loop -- given a project directory -- will list and read
		 * parse all fact files in its "/facts" subdirectory. This allows you to
		 * have multiple .iris files with your program facts. For instance you
		 * can have one file for each relation's facts as our examples show.
		 */
		final File factsDirectory = new File(projectDirectory + "/facts");
		if (factsDirectory.isDirectory()) {
			for (final File fileEntry : factsDirectory.listFiles()) {

				if (fileEntry.isDirectory())
					System.out.println("Omitting directory "
							+ fileEntry.getPath());

				else {
					Reader factsReader = new FileReader(fileEntry);
					parser.parse(factsReader);

					// Retrieve the facts and put all of them in factMap
					factMap.putAll(parser.getFacts());
				}
			}
		} else {
			System.err.println("Invalid facts directory path");
			System.exit(-1);
		}

		File rulesFile = new File(projectDirectory + "/rules.iris");
		Reader rulesReader = new FileReader(rulesFile);

		File queriesFile = new File(projectDirectory + "/queries.iris");
		Reader queriesReader = new FileReader(queriesFile);

		// Parse rules file.
		parser.parse(rulesReader);
		// Retrieve the rules from the parsed file.
		List<IRule> rules = parser.getRules();

		// Parse queries file.
		parser.parse(queriesReader);
		// Retrieve the queries from the parsed file.
		List<IQuery> queries = parser.getQueries();

		// Create a default configuration.
		Configuration configuration = new Configuration();

		// Enable Magic Sets together with rule filtering.
		configuration.programOptmimisers.add(new MagicSets());

		// Create the knowledge base.
		IKnowledgeBase knowledgeBase = new KnowledgeBase(factMap, rules,
				configuration);

		// Evaluate all queries over the knowledge base.
		for (IQuery query : queries) {
			List<IVariable> variableBindings = new ArrayList<>();
			IRelation relation = knowledgeBase.execute(query, variableBindings);

			// Output the variables.
			System.err.println("\n" + query.toString() + "\n"
					+ variableBindings);

			// Output each tuple in the relation, where the term at position i
			// corresponds to the variable at position i in the variable
			// bindings list.
			for (int i = 0; i < relation.size(); i++) {
				System.err.println(relation.get(i));
			}
			System.err.println("\n");
			relationsList.push(relation);
		}
	}

	public void printAnalyses(int which) {
		IRelation relation = relationsList.get(which);
		for (int i = 0; i < relation.size(); i++) {
			System.err.println(relation.get(i));
		}
		System.err.println("\n");
	}
	
//	public void elimDead(String file) throws IOException {
//		IRelation dead = relationsList.get(2);
//		InputStream fis = new FileInputStream(file);
//		InputStreamReader isr = new InputStreamReader(fis);
//		BufferedReader br = new BufferedReader(isr);
//		String line;
//		while ((line = br.readLine()) != null) {
//			for (int i = 0; i < dead.size(); i++) {
//				if(line.contains(dead.get(i).)) {
//					
//				}
//			}
//		}
//
//	}
}
