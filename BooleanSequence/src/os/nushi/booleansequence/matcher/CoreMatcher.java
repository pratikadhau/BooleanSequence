/***
 * The MIT License (MIT)

Copyright (c) 2015 NaturalIntelligence

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.

 */
/**
 * @author Amit Gupta
 */
package os.nushi.booleansequence.matcher;

import os.nushi.booleansequence.BooleanIdentifier;
import os.nushi.booleansequence.BooleanSequence;
import os.nushi.booleansequence.ExpressionIdentifier;
import os.nushi.booleansequence.ds.primitive.CharArrList;
import os.nushi.booleansequence.model.Counter;
import os.nushi.booleansequence.model.nodes.IterationNode;
import os.nushi.booleansequence.model.nodes.Node;

public class CoreMatcher implements Matcher{
	
	private BooleanSequence reSequence;

	public CoreMatcher() {
	}
	
	public CoreMatcher(BooleanSequence reSequence) {
		this.reSequence = reSequence;
		reset();
	}
	
	public void setSequence(BooleanSequence reSequence){
		this.reSequence = reSequence;
		reset();
	}

	public void reset(){
		for (CharArrList sublist : this.reSequence.matchedSequenceList) {
			sublist.removeAll();
		}
	}
	
	@Override
	public ExpressionIdentifier match(char... ch){
		if(ch.length < reSequence.minPathLength || (!this.reSequence.hasVariableLength && ch.length > reSequence.maxPathLength) ) return BooleanIdentifier.FAILED;
		Counter index = new Counter();
		if(this.reSequence.capture) reset();
		Node node = this.reSequence.startNode;
		for (; index.counter < ch.length; index.counter++ ) {
			Node matchingNode = match(ch,index,node);
			if(matchingNode!=null)
				node = matchingNode;
			else
				return BooleanIdentifier.FAILED;
		}
		if(node.isEndNode) return node.resultType;
		return BooleanIdentifier.FAILED;
	}
	
	private Node match(char[] ch,Counter index , Node nd) {
		for (Node node : nd.next) {
			if(node.match(ch,index)) return node.getNode();
		}
		/*if(nd instanceof IterationNode ){
			return nd.getNode();
		}else{
			for (Node node : nd.next) {
				if(node.match(ch,index)) return node.getNode();
			}
		}*/
		return null;
	}
}
