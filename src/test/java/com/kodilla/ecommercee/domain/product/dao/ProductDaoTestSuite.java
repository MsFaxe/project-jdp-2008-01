package com.kodilla.ecommercee.domain.product.dao;

import com.kodilla.ecommercee.data.CartEntity;
import com.kodilla.ecommercee.domain.cart.dao.CartDaoStub;
import com.kodilla.ecommercee.domain.group.Group;
import com.kodilla.ecommercee.domain.group.dao.GroupDao;
import com.kodilla.ecommercee.domain.product.Product;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.transaction.Transactional;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

@Transactional
@RunWith(SpringRunner.class)
@SpringBootTest
public class ProductDaoTestSuite {
    @Autowired
    private ProductDao productDao;
    @Autowired
    private CartDaoStub cartDaoStub;
    @Autowired
    private GroupDao groupDao;

    @Test
    public void testCreateAndReadProduct() {
        // Given
        Product product = new Product("test", "testProduct", 100.0);

        // When
        productDao.save(product);
        Long productId = product.getId();

        // Then
        Assert.assertEquals(product, productDao.findById(productId).orElse(null));

        // Clean-up
        productDao.deleteById(productId);
    }

    @Test
    public void testCreateAndReadProductWithGroup() {
        // Given
        Product product = new Product("test", "testProduct", 100.0);
        Group group = new Group("kurtki");

        product.addGroup(group);

        // When
        groupDao.save(group);
        productDao.save(product);

        Long productId = product.getId();
        Long groupId = group.getId();

        // Then
        Assert.assertEquals(product, productDao.findById(productId).orElse(null));
        Assert.assertEquals(product.getGroupId(), Objects.requireNonNull(productDao.findById(productId).orElse(null)).getGroupId());

        // Clean-up
        productDao.deleteById(productId);
        groupDao.deleteById(groupId);
    }

    @Test
    public void testCreateAndReadProductWithCart() {
        // Given
        Product product = new Product("test", "testProduct", 100.0);
        CartEntity cart = new CartEntity();

        cart.addProduct(product);

        // When
        cartDaoStub.save(cart);
        productDao.save(product);

        Long productId = product.getId();
        Long cartId = cart.getId();

        // Then
        Assert.assertEquals(product, productDao.findById(productId).orElse(new Product()));
        Assert.assertTrue(product.getCarts().contains(cartDaoStub.findById(cartId).orElse(new CartEntity())));

        // Clean-up
        productDao.deleteById(productId);
        cartDaoStub.deleteById(cartId);
    }

    @Test
    public void testUpdateProduct() {
        // Given
        String originalName = "test";
        String originalDesc = "testProduct";
        double originalPrice = 100.0;
        Product product = new Product(originalName, originalDesc, originalPrice);

        Group group = new Group("kurtka");
        Group newGroup = new Group("sweter");

        CartEntity cart = new CartEntity();
        CartEntity newCart = new CartEntity();

        product.addGroup(group);
        cart.addProduct(product);

        // When
        cartDaoStub.save(cart);
        cartDaoStub.save(newCart);
        groupDao.saveAll(Arrays.asList(group, newGroup));
        productDao.save(product);

        Long productId = product.getId();

        Product productDbEntry = productDao.findById(productId).orElse(new Product());

        // Then
        assertsForTestUpdateProduct(productDbEntry, originalName, originalDesc, originalPrice, 1, cart, group);

        // When (updated)
        String updatedName = "testUpdated";
        String updatedDesc = "testProductUpdated";
        double updatedPrice = 10.0;

        productDbEntry.setName(updatedName);
        productDbEntry.setDescription(updatedDesc);
        productDbEntry.setPrice(updatedPrice);

        productDbEntry.addGroup(newGroup);
        newCart.addProduct(productDbEntry);

        cartDaoStub.save(cart);
        productDao.save(productDbEntry);

        productDbEntry = productDao.findById(productId).orElse(new Product());

        // Then (updated)
        assertsForTestUpdateProduct(productDbEntry, updatedName, updatedDesc, updatedPrice, 2, newCart, newGroup);

        // Clean-up
        productDao.deleteById(productId);

        Long cartId = cart.getId();
        cartDaoStub.deleteById(cartId);

        Long groupId = group.getId();
        Long newGroupId = newGroup.getId();
        groupDao.deleteById(groupId);
        groupDao.deleteById(newGroupId);
    }

    private void assertsForTestUpdateProduct(Product product, String name, String desc, double price, int cartsCount, CartEntity presentCart, Group group) {
        Assert.assertEquals(name, product.getName());
        Assert.assertEquals(desc, product.getDescription());
        Assert.assertEquals(price, product.getPrice(), 0);
        Assert.assertEquals(cartsCount, product.getCarts().size());
        Assert.assertTrue(product.getCarts().contains(presentCart));
        Assert.assertEquals(group, product.getGroupId());
    }

    @Test
    public void testDeleteProduct() {
        // Given
        Product product = new Product("test", "testProduct", 100.0);
        Group group = new Group("kurtka");
        CartEntity cart = new CartEntity();

        product.addGroup(group);
        cart.addProduct(product);

        // When
        groupDao.save(group);
        productDao.save(product);
        cartDaoStub.save(cart);

        Long groupId = group.getId();
        Long productId = product.getId();
        Long cartId = cart.getId();

        productDao.deleteById(productId);

        // Then
        Assert.assertTrue(cartDaoStub.findById(cartId).isPresent());
        Assert.assertEquals(0, cartDaoStub.findById(cartId).get().getProductsList().size());

        Assert.assertTrue(groupDao.findById(groupId).isPresent());
        Assert.assertEquals(0, groupDao.findById(groupId).get().getProducts().size());
    }
}
