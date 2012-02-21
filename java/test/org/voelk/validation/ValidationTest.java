package org.voelk.validation;


import org.hibernate.validator.*;
import org.hibernate.validator.cfg.*;
import org.hibernate.validator.cfg.defs.*;
import org.junit.*;

import javax.validation.*;
import javax.validation.constraints.*;
import javax.validation.metadata.*;
import java.lang.annotation.*;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ValidationTest {

    private static Validator validator;

    @BeforeClass
    public static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    public void manufacturerIsNull() {
        Car car = new Car(null, "DD-AB-123", 4);

        Set<ConstraintViolation<Car>> constraintViolations =
                validator.validate(car);

        assertEquals(1, constraintViolations.size());
        assertEquals("kann nicht null sein", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void licensePlateTooShort() {
        Car car = new Car("Morris", "D", 4);

        Set<ConstraintViolation<Car>> constraintViolations =
                validator.validate(car);

        assertEquals(1, constraintViolations.size());
        assertEquals("muss zwischen 2 und 14 liegen", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void seatCountTooLow() {
        Car car = new Car("Morris", "DD-AB-123", 1);

        Set<ConstraintViolation<Car>> constraintViolations =
                validator.validate(car);

        assertEquals(1, constraintViolations.size());
        assertEquals("muss größergleich 2 sein", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void carIsValid() {
        Car car = new Car("Morris", "DD-AB-123", 2);

        Set<ConstraintViolation<Car>> constraintViolations =
                validator.validate(car);

        assertEquals(0, constraintViolations.size());
    }

    @Test
    public void testQueryingConstraints() {
        BeanDescriptor beanDescriptor = validator.getConstraintsForClass(Car.class);
        Set<PropertyDescriptor> propertyDescriptors = beanDescriptor.getConstrainedProperties();
        assertEquals(3, propertyDescriptors.size());
        PropertyDescriptor propertyDescriptorManufacturer = beanDescriptor.getConstraintsForProperty("manufacturer");
        assertTrue(propertyDescriptorManufacturer.hasConstraints());
        Set<ConstraintDescriptor<?>> constraintDescriptors = propertyDescriptorManufacturer.getConstraintDescriptors();
        assertEquals(1, constraintDescriptors.size());
    }

    @Test
    public void testProgrammaticValidatorGeneration() {
        ConstraintMapping constraintMapping = new ConstraintMapping();
        constraintMapping.type(ProgrammaticCar.class).property("manufacturer", ElementType.FIELD).constraint(new NotNullDef());
        HibernateValidatorConfiguration config = Validation.byProvider(HibernateValidator.class).configure();
        config.addMapping(constraintMapping);
        ValidatorFactory factory = config.buildValidatorFactory();
        Validator programmaticValidator = factory.getValidator();
        ProgrammaticCar car = new ProgrammaticCar(null, "", 1);
        Set<ConstraintViolation<ProgrammaticCar>> constraintViolations = programmaticValidator.validate(car);
        assertEquals(1, constraintViolations.size());
        assertEquals("kann nicht null sein", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void testProgrammaticValidatorGenerationDoesNotOverrideAnnotations() {
        ConstraintMapping constraintMapping = new ConstraintMapping();
        constraintMapping.type(Car.class).property("manufacturer", ElementType.FIELD).constraint(new NotNullDef());
        HibernateValidatorConfiguration config = Validation.byProvider(HibernateValidator.class).configure();
        config.addMapping(constraintMapping);
        ValidatorFactory factory = config.buildValidatorFactory();
        Validator programmaticValidator = factory.getValidator();
        Car car = new Car(null, "", 1);
        Set<ConstraintViolation<Car>> constraintViolations = programmaticValidator.validate(car);
        assertEquals(3, constraintViolations.size());
    }

    private class Car {
        @NotNull
        private String manufacturer;

        @NotNull
        @Size(min = 2, max = 14)
        private String licensePlate;

        @Min(2)
        private int seatCount;

        public Car(String manufacturer, String licencePlate, int seatCount) {
            this.manufacturer = manufacturer;
            this.licensePlate = licencePlate;
            this.seatCount = seatCount;
        }
    }

    private class ProgrammaticCar {
        private String manufacturer;

        private String licensePlate;

        private int seatCount;

        public ProgrammaticCar(String manufacturer, String licencePlate, int seatCount) {
            this.manufacturer = manufacturer;
            this.licensePlate = licencePlate;
            this.seatCount = seatCount;
        }
    }
}
