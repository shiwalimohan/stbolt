package com.soartech.bolt.evaluation;

public class ThreeByThreeConfig {
	private BoardLocation primeObjLoc;
	private BoardLocation refObjLoc;
	
	public ThreeByThreeConfig(BoardLocation reference, BoardLocation primary) {
		refObjLoc = reference;
		primeObjLoc = primary;
	}
	
	public ThreeByThreeConfig(int rr, int rc, int pr, int pc) {
		refObjLoc = new BoardLocation(rr, rc);
		primeObjLoc = new BoardLocation(pr, pc);
	}

	public BoardLocation getPrimeObjLoc() {
		return primeObjLoc;
	}

	public void setPrimeObjLoc(BoardLocation referenceObjLoc) {
		this.primeObjLoc = referenceObjLoc;
	}

	public BoardLocation getRefObjLoc() {
		return refObjLoc;
	}

	public void setRefObjLoc(BoardLocation primaryObjLoc) {
		this.refObjLoc = primaryObjLoc;
	}
}
