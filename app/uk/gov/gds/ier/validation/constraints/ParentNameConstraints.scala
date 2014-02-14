package uk.gov.gds.ier.validation.constraints

import uk.gov.gds.ier.validation.{FormKeys, ErrorMessages, Key}
import uk.gov.gds.ier.model.{ParentName, ParentPreviousName, InprogressOverseas}
import play.api.data.validation.{Invalid, Valid, Constraint}
import play.api.data.Mapping
import play.api.data.Forms._
import uk.gov.gds.ier.model.InprogressOverseas
import play.api.Logger

trait ParentNameConstraints extends CommonConstraints {
  self:  FormKeys
    with ErrorMessages =>
     
      
    lazy val parentNameNotOptional = Constraint[Option[ParentName]] (keys.parentName.key) {
      name =>
      if (name.isDefined) Valid
      else Invalid("Please enter their full name", 
          keys.parentName.firstName, keys.parentName.lastName, keys.parentName)
    }  
    
    lazy val parentPreviousNameNotOptionalIfHasPreviousIsTrue = Constraint[Option[ParentPreviousName]] (keys.parentPreviousName.key) {
      name => 
        if (name.isDefined) {
         if (name.get.hasPreviousName && !name.get.previousName.isDefined) {
           Invalid("Please enter their previous full name", 
               keys.parentPreviousName.previousName.firstName,
               keys.parentPreviousName.previousName.lastName)
         }
         else Valid 
        }
        else Invalid("Please answer this quesiton", keys.parentPreviousName.hasPreviousName)
    }
    
//  lazy val parentNameNotOptional = Constraint[InprogressOverseas](keys.parentName.key) {
//    application =>
//      if (application.parentName.isDefined) Valid
//      else Invalid("Please enter their full name", 
//            keys.parentName.firstName, keys.parentName.lastName, keys.parentName)
//  }
  
  lazy val parentPrevNameOptionCheck = Constraint[InprogressOverseas] (keys.parentPreviousName.key) {
    application =>
      if (application.parentPreviousName.isDefined) Valid
      else Invalid("Please answer this question", keys.parentPreviousName)
  }
  
//  lazy val parentNameNotEmpty = Constraint[ParentName](keys.parentName.key) {
//    name =>
//      if (name.firstName.trim.isEmpty() && name.lastName.trim.isEmpty()) 
//        Invalid("Please enter their full name",
//            keys.parentName.firstName, keys.parentName.lastName, keys.parentName)
//      else Valid
//  }
  
//  lazy val parentPreviousNameNotEmpty = Constraint[ParentPreviousName](keys.parentPreviousName.key) {
//    name =>
//      if (name.firstName.trim.isEmpty() && name.lastName.trim.isEmpty()) 
//        Invalid("Please enter their full name",
//            keys.parentName.firstName, keys.parentName.lastName, keys.parentName)
//      else Valid
//  }
  
  def parentFirstNameNotEmpty = Constraint[ParentName](keys.parentName.firstName.key) {
    name =>
//      name match {
//        case ParentName("", None, "") => Invalid("please enter their full name", keys.parentName)
//        case ParentName("", Some(_), "") => Invalid("please enter their first name", keys.parentName.firstName) ++
//          Invalid("please enter their last name", keys.parentName.lastName)
//        case ParentName("", Some(_), _) => Invalid("please enter their first name", keys.parentName.firstName)
//        case ParentName(_, Some(_), "") => Invalid("please enter their last name", keys.parentName.lastName)
//        case _ => Valid
//      }
      if (name.firstName.trim.isEmpty()) Invalid("Please enter their first name",
          keys.parentName.firstName)
      else Valid
  }
  
  def parentLastNameNotEmpty = Constraint[ParentName](keys.parentName.lastName.key) {
    name =>
      if (name.lastName.trim.isEmpty()) Invalid("Please enter their last name", 
          keys.parentName.lastName)
      else Valid
  }
  
  lazy val parentFirstNameNotTooLong = fieldNotTooLong[ParentName](keys.parentName.firstName,
    firstNameMaxLengthError) (_.firstName)

  lazy val parentMiddleNamesNotTooLong = fieldNotTooLong[ParentName](keys.parentName.middleNames,
    middleNameMaxLengthError) (_.middleNames.getOrElse(""))

  lazy val parentLastNameNotTooLong = fieldNotTooLong[ParentName](keys.parentName.lastName,
    lastNameMaxLengthError) (_.lastName)
  
  
  
  def parentPreviousFirstNameNotEmpty = Constraint[ParentPreviousName](keys.parentPreviousName.previousName.firstName.key) {
    name =>
//      name match {
//        case ParentPreviousName(false, _) => Valid
//        case ParentPreviousName(true, None) => Invalid("Please enter their previous name", 
//            keys.parentPreviousName.previousName.firstName, 
//            keys.parentPreviousName.previousName.lastName)
//        case ParentPreviousName(true, Some(ParentName("", Some(_), ""))) => 
//          Invalid("Please enter their previous first name", keys.parentPreviousName.previousName.firstName) ++ 
//          Invalid("Please enter their previous last name", keys.parentPreviousName.previousName.lastName)
//        case ParentPreviousName(true, Some(ParentName("", Some(_), _))) =>
//          Invalid("Please enter their previous first name", keys.parentPreviousName.previousName.firstName)
//        case ParentPreviousName(true, Some(ParentName(_, Some(_), ""))) =>
//          Invalid("Please enter their previous last name", keys.parentPreviousName.previousName.lastName)
//        case ParentPreviousName(true, Some(ParentName(_, _, _))) => Valid
//      }
      if (name.hasPreviousName) {
        if (name.previousName.isDefined && name.previousName.get.firstName.trim.isEmpty()) Invalid("Please enter their previous first name", keys.parentPreviousName.previousName.firstName)
        else Valid
      } 
      else Valid 
  }
  
  def parentPreviousLastNameNotEmpty = Constraint[ParentPreviousName](keys.parentPreviousName.previousName.lastName.key) {
    name =>
      if (name.hasPreviousName) {
      if (name.previousName.isDefined && name.previousName.get.lastName.trim.isEmpty()) Invalid("Please enter their previous last name", keys.parentPreviousName.previousName.lastName)
      else Valid
      } 
      else Valid 
  }
  
  lazy val parentPrevFirstNameNotTooLong = fieldNotTooLong[ParentPreviousName](
    keys.parentPreviousName.previousName.firstName,
    firstNameMaxLengthError) (_.previousName.map(_.firstName).getOrElse(""))

  lazy val parentPrevMiddleNamesNotTooLong = fieldNotTooLong[ParentPreviousName](
    keys.parentPreviousName.previousName.middleNames,
    middleNameMaxLengthError) (_.previousName.flatMap(_.middleNames).getOrElse(""))

  lazy val parentPrevLastNameNotTooLong = fieldNotTooLong[ParentPreviousName](
    keys.parentPreviousName.previousName.lastName,
    lastNameMaxLengthError) (_.previousName.map(_.lastName).getOrElse(""))

  
}
