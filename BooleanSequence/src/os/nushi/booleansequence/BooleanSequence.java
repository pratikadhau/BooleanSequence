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
package os.nushi.booleansequence;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import os.nushi.booleansequence.ds.primitive.CharArrList;
import os.nushi.booleansequence.ds.primitive.CharStack;
import os.nushi.booleansequence.matcher.CoreMatcher;
import os.nushi.booleansequence.matcher.ProgressiveMatcher;
import os.nushi.booleansequence.model.Counter;
import os.nushi.booleansequence.model.SequenceLength;
import os.nushi.booleansequence.model.nodes.AnyNode;
import os.nushi.booleansequence.model.nodes.LazyNode;
import os.nushi.booleansequence.model.nodes.Node;
import os.nushi.booleansequence.model.nodes.NormalNode;
import os.nushi.booleansequence.model.nodes.RangeNode;
import os.nushi.booleansequence.util.CharArrayUtil;
import os.nushi.booleansequence.util.CharUtil;

//TODO : possible improvements

//https://swtch.com/~rsc/regexp/regexp1.html
//to implement *,+ use stacking. so whenever assertion fails take the node from stack and start from back
//Lazy/Progressive check supporting in //1 cases too
 
//Support for backslash chars
/**
 * @author Amit Gupta
 *
 */
public class BooleanSequence {

	private char[] re;
	private ExpressionIdentifier expressionIdentifier;

	public BooleanSequence(String str) {
		this(str.toCharArray());
	}
	
	public BooleanSequence(String str,ExpressionIdentifier expressionIdentifier) {
		this(str.toCharArray(),expressionIdentifier);
	}
	
	public BooleanSequence(char[] re) {
		this(re, BooleanIdentifier.PASSED);
	}
	
	public BooleanSequence(char[] re,ExpressionIdentifier expressionIdentifier) {
		this.re = re;
		this.expressionIdentifier = expressionIdentifier;
		this.startNode = new NormalNode();
		this.currentNode = this.startNode;
		this.endNode = new NormalNode();
	}
	
	public Node startNode;
	private Node currentNode;
	public Node endNode;
	public boolean capture;
	private CharArrList matchedSequence;
	public List<CharArrList> matchedSequenceList = new ArrayList<CharArrList>();
	private boolean subsequencecapture;
	private boolean subsequence;
	public boolean hasVariableLength;
	Integer index;
	
	public BooleanSequence compile(){
		Node oldNode = null;
		BooleanSequence subsequence = null;
		index = 0;
		for (index=0; index< re.length; index++) {
			
			if(re[index] == '('){
				CharArrList matchedSequence = new CharArrList();
				matchedSequenceList.add(matchedSequence);//It'll store the result of captured sequence
				
				subsequence = getMeASubSequence();
				subsequence.matchedSequence = matchedSequence;
				subsequence.compile();
				
				BooleanSequenceUtil.mergeNodes(currentNode, subsequence.startNode);
				
				//forward linking
				oldNode = currentNode;
				currentNode = subsequence.endNode;
			}else if(re[index] == '|'){
				closeTheCurrentSequence();
				//Start new sequence
				currentNode = this.startNode;
			}else if(re[index] == '['){
				oldNode = forwardLinking(generateNodesForBracketSequence());
			}else if(re[index] == '?'){
				jointLinking(oldNode);
			}else if(re[index] == '.'){
				oldNode = forwardLinking(getAnyNode());
			}/*else if(re[index] == '+'){//1 or more
				//convert end node of last sequence to iteration node
				
				if(currentNode.isJointNode()){
					//Go one more step backward
					Set<Node> lastNodes = new HashSet<Node>(currentNode.last);
					currentNode.last.clear();
					for (Node node : lastNodes) {
						IterationNode iNode = getIterationNode(node,1,-1,node);
						iNode.setStartingNode(iNode);
						//update references
						for (Node lastNode : node.last) {
							lastNode.next.remove(node);
							lastNode.next.add(iNode);
						}
						currentNode.last.add(iNode);
					}
				}else{
					Node iNode = getIterationNode(currentNode,1,-1,currentNode);
					//update references
					for (Node lastNode : currentNode.last) {
						lastNode.next.remove(currentNode);
						lastNode.next.add(iNode);
					}
					currentNode = iNode;
				}

				
				hasVariableLength = true;
				subsequence = null;
			}else if(re[i] == '{'){//TODO
				int start = ++i;
				//read until } is found
				for(;re[i] != '}';i++){
					if(!CharUtil.isDigit(re[i])){
						System.out.println("Invalid Boolean Expression");
					}
				}
				
				int num = Integer.parseInt(new String(CharArrayUtil.subArray(re, start, i-1)));
			}*/else if(re[index] == '\\'){//add next char as plain Node
				char c = re[++index];
				
				if(CharUtil.isDigit(c)){
					oldNode = forwardLinking(getLazyNode(c));
				}else{
					oldNode = forwardLinking(getNode(c));
				}
			}else{
				oldNode = forwardLinking(getNode(re[index]));
			}
			
		}
		closeTheCurrentSequence();
		return this;
	}



	private void closeTheCurrentSequence() {
		if(!this.subsequence) {
			markEndNode(currentNode);
		}else{
			currentNode.next.add(endNode);
			endNode.last.add(currentNode);	
		}
	}
	
	private void markEndNode(Node parentNode) {
		parentNode.isEndNode = true;
		parentNode.resultType = expressionIdentifier;
	}

	private BooleanSequence getMeASubSequence() {
		BooleanSequence subsequence;
		int startIndex = index+1;
		
		CharStack stack = new CharStack();
		while(true){
			if(re[index] == '('){
				stack.push('(');
			}else if(re[index] == ')'){
				stack.pop();
				if(stack.size() == 0) break;
			}
			index++;
		}
		
		subsequence = new BooleanSequence(CharArrayUtil.subArray(re, startIndex,index-1));
		subsequence.subsequence = true;
		subsequence.subsequencecapture = this.capture;
		return subsequence;
	}

	/**
	 * Bracket sequence can have either normal node or range node
	 * bracket : [a-zA-Z0-9%] = 4 nodes
	 * @return
	 */
	private Set<Node> generateNodesForBracketSequence() {
		Set<Node> newNodes = new HashSet<Node>();
		for(index++;re[index] != ']';index++){
			if(re[index+1]=='-'){
				newNodes.add(getRangeNode(re[index],re[index+2]));
				index=index+2;
				continue;
			}
			newNodes.add(getNode(re[index]));
		}
		return newNodes;
	}
	
	/**
	 * old->current => old->current; old->joint; current->joint  
	 * @param oldNode
	 * @return
	 */
	private void jointLinking(Node oldNode) {
		NormalNode jointNode = new NormalNode(); //blank node
		
		currentNode.next.add(jointNode);
		jointNode.last.add(currentNode);
		
		oldNode.next.add(jointNode);
		jointNode.last.add(currentNode);
		
		currentNode = jointNode;//go forward
	}
	/**
	 * old(N1)->current(N0) new(N) => N1->old(N0)->current(N)
	 * @param newNode
	 * @return
	 */
	private Node forwardLinking(Node newNode) {
		currentNode.next.add(newNode);
		//if(!currentNode.isJointNode())
		newNode.last.add(currentNode);	
		
		//go forward
		Node  oldNode = currentNode;
		currentNode = newNode;	
		
		return oldNode;
	}
	
	
	private Node forwardLinking(Set<Node> newNodes) {
		return forwardLinking(newNodes, new NormalNode());
	}
	
	
	private Node forwardLinking(Set<Node> newNodes, Node jointNode) {
		currentNode.next.addAll(newNodes);
		for (Node node : newNodes) {
			//if(!node.isJointNode())
			node.last.add(currentNode);
			node.next.add(jointNode);
			jointNode.last.add(node);
		}
		
		//go forward		
		Node  oldNode = currentNode;
		currentNode = jointNode;	
		
		return oldNode;
	}
	
	private LazyNode getLazyNode(char c) {
		hasVariableLength = true;
		LazyNode lazynode = new LazyNode(c);
		int position = Integer.parseInt(c+"");
		lazynode.source(matchedSequenceList.get(position-1));//where to take the input from
		return lazynode;
	}
	
	/*private IterationNode getIterationNode(Node node, int min, int max, Node startNode) {
		IterationNode iterationNode = new IterationNode(node);
		iterationNode.setIterationRange(min, max);
		iterationNode.setStartingNode(startNode);
		return iterationNode;
	}*/
	
	private Node getAnyNode() {
		if(subsequencecapture) {
			Node node= new AnyNode(){
				@Override
				public boolean match(char[] ch, Counter index) {
					if(super.match(ch, index)){
						matchedSequence.add(ch[index.counter]);
						return true;
					}
					return false;
				}
			};
			return node;
		}
		return new AnyNode() ;
	}

	private Node getRangeNode(char from,char to) {
		if(subsequencecapture) {
			RangeNode node = new RangeNode(from,to){
				@Override
				public boolean match(char[] ch, Counter index) {
					if(super.match(ch, index)){
						matchedSequence.add(ch[index.counter]);
						return true;
					}
					return false;
				}
			};
			return node;
		}
		return new RangeNode(from,to);
	}
	
	private Node getNode(char ch){
		if(subsequencecapture) {
			NormalNode node = new NormalNode(ch){
				@Override
				public boolean match(char[] ch, Counter index) {
					if(super.match(ch, index)){
						matchedSequence.add(ch[index.counter]);
						return true;
					}
					return false;
				}
			};
			
			return node;
		}
		return new NormalNode(ch);
	}
	/**
	 * Removes blank nodes
	 */
	public BooleanSequence minimize(){
		Node parentNode = this.startNode;
		removeBlankNodes(parentNode);
		BooleanSequenceUtil.mergeAllDuplicateNodes(parentNode);
		System.gc();
		updatePathLength();
		return this;
	}
	
	private void removeBlankNodes(Node parentNode) {
		Set<Node> toRemove = new HashSet<Node>();
		
		for(Node node : parentNode.next){
			if(!node.next.isEmpty())
				removeBlankNodes(node);
			
			if(node.isJointNode()){
				if(node.isEndNode){
					markEndNode(parentNode);
					toRemove.add(node);
				}else{
					toRemove.add(node);
				}
			}
		}
		parentNode.next.removeAll(toRemove);
		for(Node node : toRemove){
			parentNode.next.addAll(node.next);
			for(Node nextNode : node.next){
				nextNode.last.remove(node);
				nextNode.last.addAll(node.last);
			}
		}
		BooleanSequenceUtil.mergeDuplicateNodes(parentNode.next);
	}


	public int minPathLength;
	public int maxPathLength;
	
	public void updatePathLength(){
		SequenceLength depth = BooleanSequenceUtil.calculateDepth(this);
		minPathLength = depth.min;
		maxPathLength = depth.max;
	}
	
	public BooleanSequence merge(BooleanSequence sequence){
		BooleanSequenceUtil.mergeSequences(this, sequence);
		return this;
	}
	
	public CoreMatcher getCoreMatcher(){
		return new CoreMatcher(this);
	}
	
	public ProgressiveMatcher getProgressiveMatcher(){
		return new ProgressiveMatcher(this);
	}
}

