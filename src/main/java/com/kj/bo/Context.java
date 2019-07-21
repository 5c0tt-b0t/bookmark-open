package com.kj.bo;

class Context {

	private final Strategy strategy;

	public Context(Strategy strategy){
		this.strategy = strategy;
	}

	public void executeStrategy(String[] args){
		this.strategy.execute(args);
	}

}
