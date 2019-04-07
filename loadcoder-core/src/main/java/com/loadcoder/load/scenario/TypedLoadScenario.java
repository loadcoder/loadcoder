/*******************************************************************************
 * Copyright (C) 2019 Stefan Vahlgren at Loadcoder
 * 
 * This file is part of Loadcoder.
 * 
 * Loadcoder is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Loadcoder is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package com.loadcoder.load.scenario;

public abstract class TypedLoadScenario<T> extends LoadScenario {

	private ThreadLocal<T> threadLocal;

	public abstract T createInstance();

	public abstract void loadScenario(T t);

	public final void loadScenario() {
		T t = threadLocal.get();
		loadScenario(t);
	}

	@Override
	public final void setLoad(Load l) {
		super.setLoad(l);
		threadLocal = new ThreadLocal<T>();
	}

	@Override
	public final void preThreadExecution() {
		T t = createInstance();
		threadLocal.set(t);
		preThreadExecution(t);
	}

	@Override
	public final void postThreadExecution() {
		T t = threadLocal.get();
		postThreadExecution(t);
	}

	public void preThreadExecution(T t) {
	}

	public void postThreadExecution(T t) {
	}

}
