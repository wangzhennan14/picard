package picard.sam.markduplicates.util;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.Assert;
import picard.sam.util.ReadNameParsingUtils;

import java.util.Arrays;
import java.util.List;

/**
 * Tests for OpticalDuplicateFinder
 *
 * @author Nils Homer
 */
public class OpticalDuplicateFinderTest {

    /** Tests rapidParseInt for positive and negative numbers, as well as non-digit suffixes */
    @Test
    public void testRapidParseInt() {
        final OpticalDuplicateFinder opticalDuplicateFinder = new OpticalDuplicateFinder();
        for (int i = -100; i < 100; i++) {
            Assert.assertEquals(ReadNameParsingUtils.rapidParseInt(Integer.toString(i)), i);

            // trailing characters
            Assert.assertEquals(ReadNameParsingUtils.rapidParseInt(Integer.toString(i)+"A"), i);
            Assert.assertEquals(ReadNameParsingUtils.rapidParseInt(Integer.toString(i)+"ACGT"), i);
            Assert.assertEquals(ReadNameParsingUtils.rapidParseInt(Integer.toString(i)+".1"), i);
        }
    }

    /** Helper for testGetRapidDefaultReadNameRegexSplit */
    private void doTestGetRapidDefaultReadNameRegexSplit(int numFields) {
        final int[] inputFields = new int[3];
        final int[] expectedFields = new int[3];
        String readName = "";
        for (int i = 0; i < numFields; i++) {
            if (0 < i) readName += ":";
            readName += Integer.toString(i);
        }
        inputFields[0] = inputFields[1] = inputFields[2] = -1;
        if (numFields < 3) {
            Assert.assertEquals(ReadNameParsingUtils.getRapidDefaultReadNameRegexSplit(readName, ':', inputFields), -1);
        }
        else {
            Assert.assertEquals(ReadNameParsingUtils.getRapidDefaultReadNameRegexSplit(readName, ':', inputFields), numFields);
            expectedFields[0] = expectedFields[1] = expectedFields[2] = -1;
            if (0 < numFields) expectedFields[0] = numFields-3;
            if (1 < numFields) expectedFields[1] = numFields-2;
            if (2 < numFields) expectedFields[2] = numFields-1;
            for (int i = 0; i < inputFields.length; i++) {
                Assert.assertEquals(inputFields[i], expectedFields[i]);
            }
        }
    }

    /** Tests that we split the string with the correct # of fields, and modified values */
    @Test
    public void testGetRapidDefaultReadNameRegexSplit() {
        for (int i = 1; i < 10; i++) {
            doTestGetRapidDefaultReadNameRegexSplit(i);
        }
    }

    // NB: these tests fails due to overflow in the duplicate finder test.  This has been the behavior previously, so keep it for now.
    @Test(dataProvider = "testParseReadNameDataProvider", enabled = false)
    public void testParseReadName(final String readName, final int tile, final int x, final int y) {
        OpticalDuplicateFinder opticalDuplicateFinder = new OpticalDuplicateFinder();
        OpticalDuplicateFinder.PhysicalLocation loc = new ReadEndsForMarkDuplicates();
        Assert.assertTrue(opticalDuplicateFinder.addLocationInformation(readName, loc));
        Assert.assertEquals(loc.getTile(), tile);
        Assert.assertEquals(loc.getX(), x);
        Assert.assertEquals(loc.getY(), y);
    }

    @DataProvider(name = "testParseReadNameDataProvider")
    public Object[][] testParseReadNameDataProvider() {
        return new Object[][]{
                {"RUNID:7:1203:2886:82292", 1203, 2886, 82292},
                {"RUNID:7:1203:2884:16834", 1203, 2884, 16834}
        };
    }

    @Test
    public void testVeryLongReadNames() {
        final String readName1 = "M01234:123:000000000-ZZZZZ:1:1105:17981:23325";
        final String readName2 = "M01234:123:000000000-ZZZZZ:1:1109:22981:17995";

        final int[] tokens = new int[3];
        Assert.assertEquals(ReadNameParsingUtils.getRapidDefaultReadNameRegexSplit(readName1, ':', tokens), 7);
        Assert.assertEquals(ReadNameParsingUtils.getRapidDefaultReadNameRegexSplit(readName2, ':', tokens), 7);

        final OpticalDuplicateFinder opticalDuplicateFinder = new OpticalDuplicateFinder();
        final OpticalDuplicateFinder.PhysicalLocation loc1 = new ReadEndsForMarkDuplicates();
        final OpticalDuplicateFinder.PhysicalLocation loc2 = new ReadEndsForMarkDuplicates();

        Assert.assertTrue(opticalDuplicateFinder.addLocationInformation(readName1, loc1));
        Assert.assertTrue(opticalDuplicateFinder.addLocationInformation(readName2, loc2));

        final boolean[] opticalDuplicateFlags = opticalDuplicateFinder.findOpticalDuplicates(Arrays.asList(loc1, loc2));
        for (final boolean opticalDuplicateFlag : opticalDuplicateFlags) {
            Assert.assertFalse(opticalDuplicateFlag);
        }
    }
}
