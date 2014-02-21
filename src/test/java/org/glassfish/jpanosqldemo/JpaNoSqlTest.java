/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2013 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * http://glassfish.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package org.glassfish.jpanosqldemo;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;

import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class JpaNoSqlTest {

	private EntityManagerFactory entityManagerFactory;
	private EntityManager entityManager;
	private static String oid;

	@BeforeClass
	public static void setUpClass() {
		cleanDatabase();
	}

	@AfterClass
	public static void tearDownClass() {
		cleanDatabase();
	}

	private static void cleanDatabase() {
		System.out.println("\nCleaning database.\n");

		EntityManagerFactory entityManagerFactory = Persistence
				.createEntityManagerFactory("mongodb");
		EntityManager entityManager = entityManagerFactory
				.createEntityManager();

		entityManager.getTransaction().begin();

		List<Customer> customers = entityManager.createQuery(
				"Select c from Customer c", Customer.class).getResultList();

		for (Customer customer : customers) {
			entityManager.remove(customer);
		}

		List<Order> orders = entityManager.createQuery("Select o from Order o",
				Order.class).getResultList();

		for (Order order : orders) {
			entityManager.remove(order);
		}

		entityManager.getTransaction().commit();

		entityManager.close();
		entityManagerFactory.close();
	}

	@Before
	public void setUp() {
		entityManagerFactory = Persistence
				.createEntityManagerFactory("mongodb");
		entityManager = entityManagerFactory.createEntityManager();
	}

	@After
	public void tearDown() {
		entityManager.close();
		entityManagerFactory.close();
	}

	@Test
	@InSequence(1)
	public void testPersist() {
		System.out.println("\nTesting persist() of orders and customers.\n");

		entityManager.getTransaction().begin();

		Customer customer = new Customer();
		customer.setName("AMCE");
		entityManager.persist(customer);

		Order order = new Order();
		order.setCustomer(customer);
		order.setDescription("Pinball machine");
		Address address = new Address();
		address.setStreet("17 Jane St.");
		address.setCity("Pittsburgh");
		address.setState("PA");
		address.setZip("06758");
		order.setBillingAddress(address);
		address = new Address();
		address.setStreet("17 Jane St.");
		address.setCity("Philadelphia");
		address.setState("PA");
		address.setZip("54689");
		order.setShippingAddress(address);
		order.addOrderLine(new OrderLine("machine", 2999));
		order.addOrderLine(new OrderLine("shipping", 129));
		order.addOrderLine(new OrderLine("installation", 59));
		entityManager.persist(order);

		order = new Order();
		order.setCustomer(customer);
		order.setDescription("Foosball");
		address = new Address();
		address.setStreet("7 Bank St.");
		address.setCity("Miami");
		address.setState("FL");
		address.setZip("64849");
		order.setBillingAddress(address);
		address = new Address();
		address.setStreet("17 Jane St.");
		address.setCity("Jacksonville");
		address.setState("FL");
		address.setZip("75849");
		order.setShippingAddress(address);
		order.addOrderLine(new OrderLine("machine", 500));
		order.addOrderLine(new OrderLine("balls", 5));
		order.addOrderLine(new OrderLine("shipping", 60));
		entityManager.persist(order);

		customer = new Customer();
		customer.setName("Smith");
		entityManager.persist(customer);

		order = new Order();
		order.setCustomer(customer);
		order.setDescription("Pingpong table");
		address = new Address();
		address.setStreet("7 Bank St.");
		address.setCity("Los Angeles");
		address.setState("CA");
		address.setZip("13245");
		order.setBillingAddress(address);
		address = new Address();
		address.setStreet("17 Jane St.");
		address.setCity("Hollywood");
		address.setState("CA");
		address.setZip("78943");
		order.setShippingAddress(address);
		order.addOrderLine(new OrderLine("table", 300));
		order.addOrderLine(new OrderLine("balls", 5));
		order.addOrderLine(new OrderLine("rackets", 15));
		order.addOrderLine(new OrderLine("net", 2));
		order.addOrderLine(new OrderLine("shipping", 80));
		entityManager.persist(order);

		entityManager.getTransaction().commit();

		oid = order.getId();
	}

	@Test
	@InSequence(2)
	public void testFind() {
		System.out.println("\nTesting find() by Id.\n");
		Order order = entityManager.find(Order.class, oid);
		System.out.println("\nFound order:" + order + " by its oid: " + oid
				+ "\n");
	}

	@SuppressWarnings("unchecked")
	@Test
	@InSequence(3)
	public void testQuery() {
		System.out.println("\nTesting querying.\n");

		Query query = entityManager
				.createQuery("Select o from Order o where o.totalCost > 1000");
		List<Order> orders = query.getResultList();
		System.out
				.println("\nFound orders with cost > 1,000: " + orders + "\n");

		query = entityManager
				.createQuery("Select o from Order o where o.description like 'Pinball%'");
		orders = query.getResultList();
		System.out.println("\nFound orders for pinball: " + orders + "\n");

		query = entityManager
				.createQuery("Select o from Order o join o.orderLines l where l.description = :desc");
		query.setParameter("desc", "shipping");
		orders = query.getResultList();
		System.out.println("\nFound orders with shipping charges: " + orders
				+ "\n");

		query = entityManager.createNativeQuery("db.ORDER.findOne({\"_id\":\""
				+ oid + "\"})", Order.class);
		Order order = (Order) query.getSingleResult();
		System.out.println("\nFound order using a native query: " + order
				+ "\n");
	}

	@Test
	@InSequence(4)
	public void testUpdate() {
		System.out.println("\nTesting update of order.\n");

		entityManager.getTransaction().begin();

		Order order = entityManager.find(Order.class, oid);
		order.addOrderLine(new OrderLine("handling", 55));
		order.addOrderLine(new OrderLine("tax", 300));

		entityManager.getTransaction().commit();
	}

	@Test
	@InSequence(5)
	public void testRemove() {
		System.out.println("\nTesting remove of order.\n");

		entityManager.getTransaction().begin();

		Order order = entityManager.find(Order.class, oid);
		entityManager.remove(order);

		entityManager.getTransaction().commit();
	}
}
