/* This file is part of the Joshua Machine Translation System.
 * 
 * Joshua is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or 
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package joshua.decoder.ff.tm;

import java.util.List;

/**
 * 
 * @author Lane Schwartz
 * @version $LastChangedDate$
 */
public interface GrammarFactory<Symbol> {

	/** 
	 * Extracts a grammar which contains only those rules relevant for translating the specified sentence.
	 * 
	 * @param sentence A sentence to be translated
	 * @return a grammar, structured as a trie, that represents a set of translation rules
	 */
	public TrieGrammar<Symbol,Rule> getGrammarForSentence(List<Symbol> sentence);
	
}
