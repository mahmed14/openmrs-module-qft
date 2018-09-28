/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.qft.web.controller;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifierType;
import org.openmrs.api.context.Context;
import org.openmrs.module.qft.web.util.QFTKeys;
import org.openmrs.module.qft.web.util.TSVReader;
import org.openmrs.module.commonlabtest.LabTest;
import org.openmrs.module.commonlabtest.LabTestAttribute;
import org.openmrs.module.commonlabtest.LabTestAttributeType;
import org.openmrs.module.commonlabtest.LabTestSample;
import org.openmrs.module.commonlabtest.LabTestSample.LabTestSampleStatus;
import org.openmrs.module.commonlabtest.LabTestType;
import org.openmrs.module.commonlabtest.api.CommonLabTestService;
import org.openmrs.module.commonlabtest.api.impl.CommonLabTestServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

/**
 * This class configured as controller using annotation and mapped with the URL of
 * 'module/basicmodule/basicmoduleLink.form'.
 */
@Controller
@RequestMapping(value = "module/qft/qft.form")
public class QFTFormController {
	
	/** Logger for this class and subclasses */
	protected final Log log = LogFactory.getLog(getClass());
	
	/** Success form view name */
	private final String SUCCESS_FORM_VIEW = "/module/qft/qft";
	
	@Autowired
	private CommonLabTestService commonLabTestService;
	
	/**
	 * Initially called after the formBackingObject method to get the landing form name
	 * 
	 * @return String form view name
	 */
	@RequestMapping(method = RequestMethod.GET)
	public String showForm() {
		return SUCCESS_FORM_VIEW;
	}
	
	/**
	 * All the parameters are optional based on the necessity
	 * 
	 * @param httpSession
	 * @param anyRequestObject
	 * @param errors
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST)
	public String onSubmit(HttpSession httpSession, @ModelAttribute("anyRequestObject") Object anyRequestObject,
	        BindingResult errors, @RequestParam(value = "file") MultipartFile file,ModelMap map) {
		
		String status="File Uploaded Successfully!";
		List<String> errorsList=new ArrayList<String>();
		File convFile =null;
		List<Map<String, String>> results=null;
		if (errors.hasErrors()) {
			// return error view
		}
		try {
		 convFile = new File( file.getOriginalFilename());
		file.transferTo(convFile);
		results = TSVReader.parseTestFile(convFile);
		}catch (Exception e) {
			status="File Upload failed! \n  Error Message="+e.getMessage();
			e.printStackTrace();
		} 
		if(results!=null) {
			
		
		try {
			

			LabTestType qtLabTestType = commonLabTestService.getLabTestTypeByUuid("4f4c97c8-61c3-4c4e-82bc-ef3e8abe8ffa");
			List<LabTestAttributeType> attributeTypes = commonLabTestService.getLabTestAttributeTypes(qtLabTestType, false);
		
			
			for (Map<String,String> m: results) {
				List<LabTestAttribute> attributes=new ArrayList<LabTestAttribute>();
				String patientID=m.get(QFTKeys.SubjectID.toString());
			
				if(patientID==null | patientID.isEmpty()) {
					System.out.println("Following record is not saved because of patient id being null");
					System.out.println(m);
					continue;
				}
				PatientIdentifierType identifierType = Context.getPatientService().getPatientIdentifierTypeByName("External ID");
				List<PatientIdentifierType> types=new ArrayList<>();
				types.add(identifierType);
			
				
				List<Patient> patients=Context.getPatientService().getPatients(null, patientID, types, true);
				if(patients.size()<1 || patients.get(0)==null  ) {
					System.out.println("Following record is not found with Externali ID ="+patientID);
					errorsList.add("Following record is not found with Externali ID ="+patientID);
				
					continue;
				}
				List<LabTest> labtests = commonLabTestService.getLabTests(patients.get(0), false);
				
				
				LabTest qfTest=null;
				for(LabTest lt:labtests) {
					if(lt.getLabTestType().equals(qtLabTestType)) {
						qfTest=lt;
					}
				}
				
				if(qfTest==null)
				{
					System.out.println("No test order found for Patient ID ="+patientID);
					errorsList.add("No test order found for Patient ID ="+patientID);
				
					continue;
				}else {
					List<LabTestAttribute> existedAttributes = commonLabTestService.getLabTestAttributes(qfTest.getTestOrderId());
					int unVoidedCount=0;
					for(LabTestAttribute attribute :existedAttributes) {
						if(!attribute.getVoided()) {
							unVoidedCount++;
						}
					}
					if(unVoidedCount>0) {
						System.out.println("Test Results are Already added for patient Id = "+patientID);
						errorsList.add("Test Results are Already added for patient Id = "+patientID);
						continue;
					}
					List<LabTestSample> samples = commonLabTestService.getLabTestSamples(qfTest, false);
					for(LabTestSample sample:samples) {
						sample.setStatus(LabTestSampleStatus.PROCESSED);
						commonLabTestService.saveLabTestSample(sample);
					}
					
					
				}
				for(LabTestAttributeType attributeType:attributeTypes) {
					LabTestAttribute att=new LabTestAttribute();
					att.setLabTest(qfTest);
					att.setAttributeType(attributeType);
					att.setAttributeTypeId(attributeType);
					if(attributeType.getName().equals(QFTKeys.RunNumber.toString())) {
						att.setValueReference(m.get(QFTKeys.RunNumber.toString()));
					}
					else if (attributeType.getName().equals(QFTKeys.RunDate.toString())) {
						att.setValueReference(m.get(QFTKeys.RunDate.toString()));
					}
					else if (attributeType.getName().equals(QFTKeys.NIL.toString())) {
						att.setValueReference(m.get(QFTKeys.NIL.toString()));
					}
					else if (attributeType.getName().equals(QFTKeys.TB1.toString())) {
						att.setValueReference(m.get(QFTKeys.TB1.toString()));
					}
					else if (attributeType.getName().equals(QFTKeys.TB2.toString())) {
						att.setValueReference(m.get(QFTKeys.TB2.toString()));
					}
					else if (attributeType.getName().equals(QFTKeys.MITOGEN.toString())) {
						att.setValueReference(m.get(QFTKeys.MITOGEN.toString()));
					}
					else if (attributeType.getName().equals(QFTKeys.TB1Nil.toString())) {
						att.setValueReference(m.get(QFTKeys.TB1Nil.toString()));
					}
					else if (attributeType.getName().equals(QFTKeys.TB2Nil.toString())) {
						att.setValueReference(m.get(QFTKeys.TB2Nil.toString()));
					}
					else if (attributeType.getName().equals(QFTKeys.MitogenNil.toString())) {
						att.setValueReference(m.get(QFTKeys.MitogenNil.toString()));
					}
					else if (attributeType.getName().equals(QFTKeys.Result.toString())) {
						att.setValueReference(m.get(QFTKeys.Result.toString()));
					}
					else if (attributeType.getName().equals(QFTKeys.ValidTest.toString())) {
						att.setValueReference(m.get(QFTKeys.ValidTest.toString()));
					}
					
					
					attributes.add(att);
					
				}
				

				commonLabTestService.saveLabTestAttributes(attributes);
				
			}
			
			//status="File Uploaded Successfully!";
		} catch (Exception e) {
			
			e.printStackTrace();
			//status="File Upload failed! \n"+e.getMessage();
		}
		
		}else {
			
			status ="File Parsing Error. Please contact your administration";
		}
		
		 map.put("errorsList", errorsList);
		 map.put("status", status);
		
		return SUCCESS_FORM_VIEW;
	}
}
