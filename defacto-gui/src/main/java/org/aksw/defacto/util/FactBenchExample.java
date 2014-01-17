/**
 * 
 */
package org.aksw.defacto.util;

import org.aksw.defacto.model.DefactoModel;

import com.hp.hpl.jena.graph.Triple;

/**
 * Simple wrapper for a fact and its corresponding DeFactoModel object.
 * @author Lorenz Buehmann
 *
 */
public class FactBenchExample implements Comparable<FactBenchExample>{

	Triple triple;
	String fact;
	DefactoModel model;
	
	public FactBenchExample(Triple triple, String fact, DefactoModel model) {
		this.triple = triple;
		this.fact = fact;
		this.model = model;
	}
	
	/**
	 * @return the triple
	 */
	public Triple getTriple() {
		return triple;
	}
	
	/**
	 * @return the fact
	 */
	public String getFact() {
		return fact;
	}
	
	/**
	 * @return the model
	 */
	public DefactoModel getModel() {
		return model;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return fact;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((fact == null) ? 0 : fact.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FactBenchExample other = (FactBenchExample) obj;
		if (fact == null) {
			if (other.fact != null)
				return false;
		} else if (!fact.equals(other.fact))
			return false;
		return true;
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(FactBenchExample o) {
		return this.fact.compareTo(o.getFact());
	}
	
	
}
