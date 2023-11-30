package fr.orsay.lri.varna.models.rna.pseudoknots;

public class FactoryCCArea extends AbstractFactoryCC{

	public ConnectedComponent buildCC() {
		return new ConnectedComponentArea();
	}

}
