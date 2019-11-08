package com.example.getstarted.basicactions;

import com.example.getstarted.objects.Book;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

public class UnitTests {
    @Test
    public void positiveTest() {
        Assert.assertEquals(1, 1);
    }

    @Test
    @Ignore
    public void negativeTest() {
        Assert.assertEquals(1, 2);
    }

    @Test
    public void bookTest() {
        String title = "The Lord of the Rings";
        String year = "1937";
        String description = "There and back again";
        String id = "1337";
        String creator = "Christos";
        String author = "J.R.R. Tolkien";
        Book book = new Book.Builder()
                .author(author)
                .createdBy(creator)
                .createdById(id)
                .description(description)
                .publishedDate(year)
                .title(title)
                .objectKey("")
                .mediaUrl("")
                .build();

        String toString = "Title: " + title + ", Author: " + author + ", Published date: " + year
                + ", Added by: " + creator;

        Assert.assertEquals(toString, book.toString());
    }
}
