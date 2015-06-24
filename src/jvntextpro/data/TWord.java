/*
 Copyright (C) 2010 by
 * 
 * 	Cam-Tu Nguyen 
 *  ncamtu@ecei.tohoku.ac.jp or ncamtu@gmail.com
 *
 *  Xuan-Hieu Phan  
 *  pxhieu@gmail.com 
 *
 *  College of Technology, Vietnamese University, Hanoi
 * 	Graduate School of Information Sciences, Tohoku University
 *
 * JVnTextPro-v.2.0 is a free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation; either version 2 of the License,
 * or (at your option) any later version.
 *
 * JVnTextPro-v.2.0 is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with  JVnTextPro-v.2.0); if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */

package jvntextpro.data;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;

/**
 * The Class TWord.
 */
public class TWord {
	
	/** The token. */
	private String token;
	
	/** The tag. */
	private int primary_tagindex = 0; //-1 if this word is not tagged	
	
	// this is necessary for further processing
	// Example: in noun phase chunking
	// we may have information about pos tag (secondary tag)
	// chunking tags are main tags
	
	//or named entity recognition
	// we may have information about pos tag, chunk phase, verb phase,  (secondary tags) etc.
		
	/** The secondary tags. */
	private ArrayList<String> tags = null; 
	
	//constructors
	/**
	 * Instantiates a new t word.
	 *
	 * @param _word the _word
	 * @param _tag the _tag
	 */
	public TWord(String _word, String _tag){
		tags = new ArrayList<String>(); 
		token = _word.replaceAll(" ","_");;
		tags.add(_tag);		
		primary_tagindex = 0;
	}
	
	/**
	 * Initialize a tagged word, which may come with secondary tags.
	 * Example: NER tags are primary tags for NER, but
	 * each token may have POS tag as secondary tags.
	 * 
	 * @param _word
	 * @param _tags: an array of tags including the main tag and secondary tags.
	 * @param primary_index index of the primary tag in the _tags array
	 */
	public TWord(String _word, String [] _tags, int primary_index){
		tags = new ArrayList<String>();
		for (String tag : _tags)
			tags.add(tag);
		
		this.primary_tagindex = primary_index;
	}
	
	/**
	 * Instantiates a new t word.
	 *
	 * @param _word the _word
	 */
	public TWord(String _word){
		token = _word.replaceAll(" ","_");		
	}
	
	//get methods
	/**
	 * Gets the word.
	 *
	 * @return the word
	 */
	public String getWord(){
		return token;
	}
	
	/**
	 * Gets the tag.
	 *
	 * @return the tag
	 */
	public String getTag(){
		return tags.get(primary_tagindex);
	}
	
	/**
	 * Sets the tag.
	 *
	 * @param t the new tag
	 */
	public void setTag(String t){
		if (primary_tagindex == -1) { 
			tags.add(t);
			primary_tagindex = tags.size() - 1;
		}
		
		tags.set(primary_tagindex, t);
	}
	
	
	/**
	 * Set the index of the primary tag in the tag list.
	 * @param index
	 * @throws IndexOutOfBoundsException
	 */
	public void setPrimaryIndex(int index) throws IndexOutOfBoundsException{
	/*	System.out.println(index);
		if (index < 0 || index > tags.size() || index != -1)
			throw new IndexOutOfBoundsException("Primary index must be -1 for untagged token or between 0 and " + tags.size());*/
		this.primary_tagindex = index;
	}
	
	/**
	 * Gets the secondary tag.
	 *
	 * @param i the index in the tags list.
	 * @return the secondary tag
	 */
	public String getSecondaryTag(int i){
		// i is the predefined position of secondary tag
		// For example: in named entity recognition (which are main tag)
		// i=0 can be used for POS tag
		// i=1 can be used for noun phase tag, etc...
		if (i > tags.size() || i < 0 || i == primary_tagindex)
			return null;
		
		return tags.get(i);
	}
	
	//DEBUG
	/**
	 * Prints the.
	 */
	public void print(){
		System.out.println(this.toString());
	}
	
	/**
	 * Prints the.
	 *
	 * @param out the out
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void print(Writer out) throws IOException{
		out.write(this.toString());
	}
	
	/**
	 * Convert this object into string representation.
	 * The primary tag will be in the last position.
	 */
	public String toString(String separator){
		String output = token;
		for (int i = 0; i < tags.size(); ++i) {
			if (i == primary_tagindex) continue;
			output += separator + tags.get(i);
		}
		output += separator + tags.get(primary_tagindex);
		return output;
	}
	
	public String toString() {
		return toString("/");
	}
}
