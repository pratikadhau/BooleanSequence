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

package os.nushi.booleansequence.ds.primitive;

public class CharIntMultiMap
{
	private int[] values;
	private char[] keys;
	private int lastElementPosition = -1;
	private int sizeToIncrement = 5;
	
	public CharIntMultiMap(){
		keys = new char[sizeToIncrement];
		values = new int[sizeToIncrement];
	}
	
	public CharIntMultiMap( int initialSize){
		keys = new char[initialSize];
		values = new int[initialSize];
	}
	
	public void setSizeToIncrement(int sizeToIncrement){
		this.sizeToIncrement = sizeToIncrement;
	}
	
	public int getValue(int position){
		return values[position];
	}
	
	public char getKey(int position){
		return keys[position];
	}
	
	public void updateValue(int position, int newValue){
		values[position] = newValue;
	}
	
	public void updateKey(int position, char newKey){
		keys[position] = newKey;
	}
	
	public int size(){
		return lastElementPosition + 1;
	}
	
	public char lastKeyInserted(){
		if(lastElementPosition == -1) return '\u0000';
		return keys[lastElementPosition];
	}
	
	public int lastValueInserted(){
		return values[lastElementPosition];
	}
	
	public int[] getValues(){
		return values;
	}
	
	public char[] getKeys(){
		return keys;
	}
	
	public void add(char key,int value){
		ensureCapacity(++lastElementPosition);
		keys[lastElementPosition]=key;
		values[lastElementPosition] = value;
	}
	
	/**
	 * Remove an element from specific position. If it is at the end of map, map size will be reduce by 1
	 * @param index
	 *//*
	public void remove(int index){
		if(lastElementPosition == index){
			lastElementPosition -=1;
		}
		keys[index] = '\u0000';
	}*/
	
	private void ensureCapacity(int position) {
		if (position >= values.length){
			int[] newValues = new int[values.length + sizeToIncrement];
			char[] newKeys = new char[keys.length + sizeToIncrement];
			if(values.length > 20){
				//using JIT/JNI way for large arrays
				System.arraycopy(values, 0, newValues, 0, values.length);
				System.arraycopy(keys, 0, newKeys, 0, keys.length);
			}else{
				for(int i = 0; i < values.length; i++){
					newKeys[i] = keys[i]; 
					newValues[i] = values[i];
			    }
			}
			keys = newKeys;
			values = newValues;
			
		}
	}

	public void removeLastItem() {
		//keys[lastElementPosition] = '\u0000';
		lastElementPosition -=1;
		
	}

	public CharIntMultiMap subMap(int from, int length){
		CharIntMultiMap newMap = new CharIntMultiMap(length);
		if(length == 1){newMap.add(keys[from],values[from]);return newMap;}
		for(int i = from; i < from+length; i++){
			newMap.add(keys[i],values[i]); 
	    }
		return newMap;
	}

	public CharIntMultiMap subMap(int length) {
		return subMap(0,length);
	}
	
	public CharIntMultiMap subMapFrom(int from) {
		return subMap(from,size() - from);
	}
	
	@Override
	public String toString() {
		return new String(keys,0,lastElementPosition+1);
	}
}
