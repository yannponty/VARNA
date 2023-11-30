package fr.orsay.lri.varna.models.rna.pseudoknots;

public class FactoryCCBP extends AbstractFactoryCC{

	public ConnectedComponent buildCC() {
		return new ConnectedComponentBP();
	}
}
