/*
* This file is part of the INTO-CPS toolchain.
*
* Copyright (c) 2017-CurrentYear, INTO-CPS Association,
* c/o Professor Peter Gorm Larsen, Department of Engineering
* Finlandsgade 22, 8200 Aarhus N.
*
* All rights reserved.
*
* THIS PROGRAM IS PROVIDED UNDER THE TERMS OF GPL VERSION 3 LICENSE OR
* THIS INTO-CPS ASSOCIATION PUBLIC LICENSE VERSION 1.0.
* ANY USE, REPRODUCTION OR DISTRIBUTION OF THIS PROGRAM CONSTITUTES
* RECIPIENT'S ACCEPTANCE OF THE OSMC PUBLIC LICENSE OR THE GPL 
* VERSION 3, ACCORDING TO RECIPIENTS CHOICE.
*
* The INTO-CPS toolchain  and the INTO-CPS Association Public License 
* are obtained from the INTO-CPS Association, either from the above address,
* from the URLs: http://www.into-cps.org, and in the INTO-CPS toolchain distribution.
* GNU version 3 is obtained from: http://www.gnu.org/copyleft/gpl.html.
*
* This program is distributed WITHOUT ANY WARRANTY; without
* even the implied warranty of  MERCHANTABILITY or FITNESS FOR
* A PARTICULAR PURPOSE, EXCEPT AS EXPRESSLY SET FORTH IN THE
* BY RECIPIENT SELECTED SUBSIDIARY LICENSE CONDITIONS OF
* THE INTO-CPS ASSOCIATION.
*
* See the full INTO-CPS Association Public License conditions for more details.
*/

/*
* Author:
*		Kenneth Lausdahl
*		Casper Thule
*/
package org.intocps.orchestration.coe;

import fi.iki.elonen.NanoHTTPD;
import org.apache.commons.io.IOUtils;
import org.intocps.fmi.*;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.*;
import java.net.URI;
import java.nio.file.Files;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.IOException;
import java.net.URI;

/**
 * Created by kel on 01/09/16.
 */
@RunWith(PowerMockRunner.class) public class DerivativeInOutTest
		extends BasicTest
{

	@After public void cleanup()
	{
		FmuFactory.customFactory = null;

	}

	@Test public void test() throws IOException, NanoHTTPD.ResponseException
	{
		FmuFactory.customFactory = new IFmuFactory()
		{
			@Override public boolean accept(URI uri)
			{
				return true;
			}

			@Override public IFmu instantiate(File sessionRoot, URI uri)
					throws Exception
			{
				IFmu fmu = mock(IFmu.class);
				when(fmu.isValid()).thenReturn(true);

				IFmiComponent comp = mock(IFmiComponent.class);
				when(fmu.instantiate(anyString(), anyString(), anyBoolean(), anyBoolean(), any())).thenReturn(comp);

				compMock(fmu, comp);

				String modelDescriptionPath;
				if (uri.toASCIIString().contains("watertank-c"))
				{
					modelDescriptionPath = "src/test/resources/derivativeInOutTest/watertank-c/modelDescription.xml";

					when(comp.getReal(new long[] {
							2 })).thenReturn(new FmuResult<>(Fmi2Status.OK, new double[] {
							0.0 }));

					when(comp.getReal(new long[] { 2,
							6 })).thenReturn(new FmuResult<>(Fmi2Status.OK, new double[] {
							0.0, 0.0 }));
					when(comp.getReal(new long[] { 6,
							2 })).thenReturn(new FmuResult<>(Fmi2Status.OK, new double[] {
							0.0, 0.0 }));

					when(comp.getRealOutputDerivatives(new long[] { 6, 6, 6, 6,
							2, 2, 2, 2 }, new int[] { 1, 2, 3, 4, 1, 2, 3,
							4 })).thenReturn(new FmuResult<>(Fmi2Status.OK, new double[] {
							6.1, 6.2, 6.3, 6.4, 2.1, 2.2, 2.3, 2.4 }));

					when(comp.getRealOutputDerivatives(new long[] { 2, 2, 2, 2,
							6, 6, 6, 6 }, new int[] { 1, 2, 3, 4, 1, 2, 3,
							4 })).thenReturn(new FmuResult<>(Fmi2Status.OK, new double[] {
							2.1, 2.2, 2.3, 2.4, 6.1, 6.2, 6.3, 6.4 }));
				} else
				{
					modelDescriptionPath = "src/test/resources/derivativeInOutTest/watertankcontroller-c/modelDescription.xml";
					when(comp.getBooleans(new long[] {
							4 })).thenReturn(new FmuResult<>(Fmi2Status.OK, new boolean[] {
							true }));

					when(comp.setRealInputDerivatives(any(), any(), any())).thenReturn(Fmi2Status.Fatal);

					when(comp.setRealInputDerivatives(new long[] { 3, 3, 5,
							5 }, new int[] { 2, 1, 2, 1 }, new double[] { 0.0,
							0.0, 0.0, 0.0 })).thenReturn(Fmi2Status.OK);
					when(comp.setRealInputDerivatives(new long[] { 5, 5, 3,
							3 }, new int[] { 2, 1, 2, 1 }, new double[] { 0.0,
							0.0, 0.0, 0.0 })).thenReturn(Fmi2Status.OK);

					when(comp.setRealInputDerivatives(new long[] { 3, 3, 3, 5,
							5, 5 }, new int[] { 2, 1, 3, 2, 1,
							3 }, new double[] { 2.2, 2.1, 2.3, 6.2, 6.1, 6.3 })).thenReturn(Fmi2Status.OK);
				}

				final InputStream md = new ByteArrayInputStream(IOUtils.toByteArray(new File(modelDescriptionPath.replace('/', File.separatorChar)).toURI()));
				when(fmu.getModelDescription()).thenReturn(md);
				return fmu;
			}

		};

		test("/derivativeInOutTest/config.json", 0, 30);

	}

	private void compMock(IFmu fmu, IFmiComponent comp)
			throws FmuInvocationException, InvalidParameterException
	{

		when(comp.getFmu()).thenReturn(fmu);

		//		Fmi2Status setDebugLogging(boolean var1, String[] var2) throws FmuInvocationException;
		when(comp.setDebugLogging(anyBoolean(), any())).thenReturn(Fmi2Status.OK);

		//		Fmi2Status setupExperiment(boolean var1, double var2, double var4, boolean var6, double var7) throws FmuInvocationException;
		when(comp.setupExperiment(anyBoolean(), anyDouble(), anyDouble(), anyBoolean(), anyDouble())).thenReturn(Fmi2Status.OK);
		//		Fmi2Status enterInitializationMode() throws FmuInvocationException;
		when(comp.enterInitializationMode()).thenReturn(Fmi2Status.OK);
		//		Fmi2Status exitInitializationMode() throws FmuInvocationException;
		when(comp.exitInitializationMode()).thenReturn(Fmi2Status.OK);
		//		Fmi2Status reset() throws FmuInvocationException;
		//		when(comp.reset());
		//		Fmi2Status setRealInputDerivatives(long[] var1, int[] var2, double[] var3) throws FmuInvocationException;
		//when(comp.setRealInputDerivatives(any(), any(), any())).thenReturn(Fmi2Status.OK);
		//		FmuResult<double[]> getRealOutputDerivatives(long[] var1, int[] var2) throws FmuInvocationException;
		//
		//		FmuResult<double[]> getDirectionalDerivative(long[] var1, long[] var2, double[] var3) throws FmuInvocationException;
		//
		//		Fmi2Status doStep(double var1, double var3, boolean var5) throws FmuInvocationException;
		when(comp.doStep(anyDouble(), anyDouble(), anyBoolean())).thenReturn(Fmi2Status.OK);
		//		FmuResult<double[]> getReal(long[] var1) throws FmuInvocationException;
		//
		//		FmuResult<int[]> getInteger(long[] var1) throws FmuInvocationException;
		//
		//		FmuResult<boolean[]> getBooleans(long[] var1) throws FmuInvocationException;
		//
		//		FmuResult<String[]> getStrings(long[] var1) throws FmuInvocationException;
		//
		//		Fmi2Status setBooleans(long[] var1, boolean[] var2) throws InvalidParameterException, FmiInvalidNativeStateException;
		when(comp.setBooleans(any(), any())).thenReturn(Fmi2Status.OK);
		//		Fmi2Status setReals(long[] var1, double[] var2) throws InvalidParameterException, FmiInvalidNativeStateException;
		when(comp.setReals(any(), any())).thenReturn(Fmi2Status.OK);
		//		Fmi2Status setIntegers(long[] var1, int[] var2) throws InvalidParameterException, FmiInvalidNativeStateException;
		when(comp.setIntegers(any(), any())).thenReturn(Fmi2Status.OK);
		//		Fmi2Status setStrings(long[] var1, String[] var2) throws InvalidParameterException, FmiInvalidNativeStateException;
		when(comp.setStrings(any(), any())).thenReturn(Fmi2Status.OK);
		//		FmuResult<Boolean> getBooleanStatus(Fmi2StatusKind var1) throws FmuInvocationException;
		//
		//		FmuResult<Fmi2Status> getStatus(Fmi2StatusKind var1) throws FmuInvocationException;
		//
		//		FmuResult<Integer> getIntegerStatus(Fmi2StatusKind var1) throws FmuInvocationException;
		//
		//		FmuResult<Double> getRealStatus(Fmi2StatusKind var1) throws FmuInvocationException;
		//
		//		FmuResult<String> getStringStatus(Fmi2StatusKind var1) throws FmuInvocationException;
		//
		//		Fmi2Status terminate() throws FmuInvocationException;
		when(comp.terminate()).thenReturn(Fmi2Status.OK);
		//		void freeInstance() throws FmuInvocationException;
		//		when(comp.freeInstance());
		//		FmuResult<IFmiComponentState> getState() throws FmuInvocationException;
		//
		//		Fmi2Status setState(IFmiComponentState var1) throws FmuInvocationException;
		//
		//		Fmi2Status freeState(IFmiComponentState var1) throws FmuInvocationException;
		//
		//		boolean isValid();
		when(comp.isValid()).thenReturn(true);
		//		FmuResult<Double> getMaxStepSize() throws FmiInvalidNativeStateException;
		when(comp.getMaxStepSize()).thenReturn(new FmuResult<Double>(Fmi2Status.Discard, 0.0));
	}
}