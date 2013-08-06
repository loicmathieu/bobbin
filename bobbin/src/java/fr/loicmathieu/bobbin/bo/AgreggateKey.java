package fr.loicmathieu.bobbin.bo;

import java.io.Serializable;
import java.lang.Thread.State;

/**
 * This BO represent the agregation key of the lines of a thread stack.
 * Use to represent statistical datas around lines of thread stack.
 * 
 * @author lmathieu
 *
 */
public class AgreggateKey implements Serializable, Comparable<AgreggateKey> {
	private static final long serialVersionUID = 1392057413060931145L;

	private String aggregate;
	private State threadState;

	public String getAggregate() {
		return aggregate;
	}
	public void setAggregate(String aggregate) {
		this.aggregate = aggregate;
	}
	public State getThreadState() {
		return threadState;
	}
	public void setThreadState(State threadState) {
		this.threadState = threadState;
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((aggregate == null) ? 0 : aggregate.hashCode());
		result = prime * result + ((threadState == null) ? 0 : threadState.hashCode());
		return result;
	}


	/**
	 * @inheritDoc
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		AgreggateKey other = (AgreggateKey) obj;
		if (aggregate == null) {
			if (other.aggregate != null) {
				return false;
			}
		}
		else if (!aggregate.equals(other.aggregate)) {
			return false;
		}
		if (threadState != other.threadState) {
			return false;
		}
		return true;
	}


	/**
	 * @inheritDoc
	 */
	@Override
	public String toString() {
		return "AgreggateKey [aggregate=" + aggregate + ", threadState=" + threadState + "]";
	}


	/**
	 * @inheritDoc
	 */
	@Override
	public int compareTo(AgreggateKey other) {
		return aggregate.compareTo(other.getAggregate());
	}


}
