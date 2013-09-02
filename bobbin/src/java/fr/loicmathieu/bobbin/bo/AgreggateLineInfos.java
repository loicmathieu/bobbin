package fr.loicmathieu.bobbin.bo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * This BO represent agreggate infos around the line of a thread stack.
 * Use to represent statistical datas around lines of thread stack.
 * 
 * @author lmathieu
 *
 */
public class AgreggateLineInfos implements Serializable, Comparable<AgreggateLineInfos> {
	private static final long serialVersionUID = -797579456991493917L;

	private AgreggateKey key;
	private List<Line> resultingLines = new ArrayList<>();

	/**
	 * @inheritDoc
	 */
	@Override
	public String toString() {
		return "AgreggateLineInfos [key=" + key + ", nbLines=" + resultingLines.size() + "]";
	}

	public AgreggateKey getKey() {
		return key;
	}
	public void setKey(AgreggateKey key) {
		this.key = key;
	}
	public List<Line> getResultingLines() {
		return resultingLines;
	}
	public void setResultingLines(List<Line> resultingLines) {
		this.resultingLines = resultingLines;
	}


	/**
	 * @inheritDoc
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((key == null) ? 0 : key.hashCode());
		result = prime * result + ((resultingLines == null) ? 0 : resultingLines.hashCode());
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
		AgreggateLineInfos other = (AgreggateLineInfos) obj;
		if (key == null) {
			if (other.key != null) {
				return false;
			}
		}
		else if (!key.equals(other.key)) {
			return false;
		}
		if (resultingLines == null) {
			if (other.resultingLines != null) {
				return false;
			}
		}
		else if (!resultingLines.equals(other.resultingLines)) {
			return false;
		}
		return true;
	}

	/**
	 * We want to sort lineInfos on the more lines first
	 * @inheritDoc
	 */
	@Override
	public int compareTo(AgreggateLineInfos o) {
		return o.getResultingLines().size() - this.resultingLines.size();
	}


}
