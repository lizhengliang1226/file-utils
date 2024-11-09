package com.lzl.test;

import com.lzl.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;

/**
 * 文件id转换单元测试
 *
 * @author lzl
 * @version 1.0
 * @since 2024/11/09
 */
@RunWith(MockitoJUnitRunner.class)
public class TransFileIdTest {

    @InjectMocks
    private FileUtils fileUtils;

    @Before
    public void setUp() {
        // Any setup code if needed
    }

    @Test
    public void testGetId_NormalNameWithCAtEnd_ReturnsCorrectId() {
        String name = "225544.xyz fc2ppv-4385140";
        String id = fileUtils.getId(name);
        assertEquals("FC2-PPV-4385140", id);
    }

    @Test
    public void testGetId_NormalNameWithoutCAtEnd_ReturnsCorrectId() {
        String name = "madoubt 936928.xyz fc2ppv-4557918";
        String id = fileUtils.getId(name);
        assertEquals("FC2-PPV-4557918", id);
    }

    @Test
    public void testGetId_FC2NameWithPPV_ReturnsCorrectId() {
        String name = "dhsa@ABF-767_C";
        String id = fileUtils.getId(name);
        assertEquals("ABF-767-C", id);
    }

    @Test
    public void testGetId_FC2NameWithoutPPV_ReturnsCorrectId() {
        String name = "FC2-1234567";
        String id = fileUtils.getId(name);
        assertEquals("FC2-PPV-1234567", id);
    }

    @Test
    public void testGetId_NonMatchingPatterns_ReturnsEmptyId() {
        String name = "NonMatchingName";
        String id = fileUtils.getId(name);
        assertEquals("", id);
    }
}
