package io.realm;

import android.test.AndroidTestCase;

import java.lang.reflect.InvocationTargetException;
import java.util.Date;

import io.realm.entities.AllTypes;
import io.realm.entities.AllTypesIndex;
import io.realm.entities.Dog;
import io.realm.entities.NonLatinFieldNames;
import io.realm.entities.Owner;
import io.realm.entities.StringOnly;

public class RealmQueryTest extends AndroidTestCase{

    protected final static int TEST_DATA_SIZE = 10;

    protected Realm testRealm;

    private final static String FIELD_STRING = "columnString";
    private final static String FIELD_LONG = "columnLong";
    private final static String FIELD_FLOAT = "columnFloat";
    private final static String FIELD_LONG_KOREAN_CHAR = "델타";
    private final static String FIELD_LONG_GREEK_CHAR = "Δέλτα";
    private final static String FIELD_FLOAT_KOREAN_CHAR = "베타";
    private final static String FIELD_FLOAT_GREEK_CHAR = "βήτα";

    @Override
    protected void setUp() throws Exception {
        Realm.deleteRealmFile(getContext());
        testRealm = Realm.getInstance(getContext());
    }

    @Override
    protected void tearDown() throws Exception {
        if (testRealm != null)
            testRealm.close();
    }

    // Populate testing data for query testing which will be used for AllTypes and AllTypesIndex.
    // Param clazz only takes AllTypes or AllTypesIndex for now
    // So please keep:
    // 1. AllTypesIndex always has the same field names, getters and setters with AllTypes
    // 2. Use reflection to set/get data
    // TODO: Switch to DynamicRealmObject when it is ready
    private void populateTestRealm(int objects, Class clazz) {
        testRealm.beginTransaction();
        testRealm.allObjects(AllTypes.class).clear();
        for (int i = 0; i < objects; ++i) {
            try {
                RealmObject allTypes = testRealm.createObject(clazz);
                clazz.getMethod("setColumnBoolean", boolean.class).invoke(allTypes, (i % 3) == 0);
                clazz.getMethod("setColumnBinary", byte[].class).invoke(allTypes, new byte[]{1, 2, 3});
                clazz.getMethod("setColumnDate", Date.class).invoke(allTypes, new Date());
                clazz.getMethod("setColumnDouble", double.class).invoke(allTypes, 3.1415);
                clazz.getMethod("setColumnFloat", float.class).invoke(allTypes, 1.234567f + i);
                clazz.getMethod("setColumnString", String.class).invoke(allTypes, "test data " + i);
                clazz.getMethod("setColumnLong", long.class).invoke(allTypes, i);
            } catch (NoSuchMethodException e) {
                fail();
            } catch (InvocationTargetException e) {
                fail();
            } catch (IllegalAccessException e) {
                fail();
            }
        }
        testRealm.commitTransaction();
    }

    private void populateNonLatinTestRealm(int objects) {
        testRealm.beginTransaction();
        testRealm.allObjects(NonLatinFieldNames.class).clear();
        for (int i = 0; i < objects; ++i) {
            NonLatinFieldNames nonLatinFieldNames = testRealm.createObject(NonLatinFieldNames.class);
            nonLatinFieldNames.set델타(i);
            nonLatinFieldNames.setΔέλτα(i);
            nonLatinFieldNames.set베타(1.234567f + i);
            nonLatinFieldNames.setΒήτα(1.234567f + i);
        }
        testRealm.commitTransaction();
    }

    private void populateTestRealm(Class clazz) {
        populateTestRealm(TEST_DATA_SIZE, clazz);
    }

    // Test query 'between' with long and String fields
    public void testRealmQueryBetween() {
        doTestRealmQueryBetween(AllTypes.class);
    }

    // Test query 'between' with indexed long and String fields
    public void testRealmQueryBetweenWithIndex() {
        doTestRealmQueryBetween(AllTypesIndex.class);
    }

    private void doTestRealmQueryBetween(Class clazz) {
        final int TEST_OBJECTS_COUNT = 200;
        populateTestRealm(TEST_OBJECTS_COUNT, clazz);

        RealmResults resultList = testRealm.where(clazz).between(FIELD_LONG, 0, 9).findAll();
        assertEquals(10, resultList.size());

        resultList = testRealm.where(clazz).beginsWith(FIELD_STRING, "test data ").findAll();
        assertEquals(TEST_OBJECTS_COUNT, resultList.size());

        resultList = testRealm.where(clazz).beginsWith(FIELD_STRING, "test data 1")
                .between(FIELD_LONG, 2, 20).findAll();
        assertEquals(10, resultList.size());

        resultList = testRealm.where(clazz).between(FIELD_LONG, 2, 20)
                .beginsWith(FIELD_STRING, "test data 1").findAll();
        assertEquals(10, resultList.size());
    }

    // Test query 'greaterThan' with float and String fields
    public void testRealmQueryGreaterThan() {
        doTestRealmQueryGreaterThan(AllTypes.class);
    }

    // Test query 'greaterThan' with non-indexed float and indexed String fields
    public void testRealmQueryGreaterThanWithIndex() {
        doTestRealmQueryGreaterThan(AllTypesIndex.class);
    }

    private void doTestRealmQueryGreaterThan(Class clazz) {
        final int TEST_OBJECTS_COUNT = 200;
        populateTestRealm(TEST_OBJECTS_COUNT, clazz);

        RealmResults resultList = testRealm.where(clazz)
                .greaterThan(FIELD_FLOAT, 10.234567f).findAll();
        assertEquals(TEST_OBJECTS_COUNT - 10, resultList.size());

        resultList = testRealm.where(clazz).beginsWith(FIELD_STRING, "test data 1")
                .greaterThan(FIELD_FLOAT, 50.234567f).findAll();
        assertEquals(TEST_OBJECTS_COUNT - 100, resultList.size());

        RealmQuery query = testRealm.where(clazz).greaterThan(FIELD_FLOAT, 11.234567f);
        resultList = query.between(FIELD_LONG, 1, 20).findAll();
        assertEquals(10, resultList.size());
    }

    // Test query 'greaterThanOrEqualTo' with float and String fields
    public void testRealmQueryGreaterThanOrEqualTo() {
        doTestRealmQueryGreaterThanOrEqualTo(AllTypes.class);
    }

    // Test query 'greaterThanOrEqualTo' with non-indexed float and indexed String fields
    public void testRealmQueryGreaterThanOrEqualToWithIndex() {
        doTestRealmQueryGreaterThanOrEqualTo(AllTypesIndex.class);
    }

    private void doTestRealmQueryGreaterThanOrEqualTo(Class clazz) {
        final int TEST_OBJECTS_COUNT = 200;
        populateTestRealm(TEST_OBJECTS_COUNT, clazz);

        RealmResults resultList = testRealm.where(clazz)
                .greaterThanOrEqualTo(FIELD_FLOAT, 10.234567f).findAll();
        assertEquals(TEST_OBJECTS_COUNT - 9, resultList.size());

        resultList = testRealm.where(clazz).beginsWith(FIELD_STRING, "test data 1")
                .greaterThanOrEqualTo(FIELD_FLOAT, 50.234567f).findAll();
        assertEquals(TEST_OBJECTS_COUNT - 100, resultList.size());

        RealmQuery query = testRealm.where(clazz)
                .greaterThanOrEqualTo(FIELD_FLOAT, 11.234567f);
        query = query.between(FIELD_LONG, 1, 20);

        resultList = query.beginsWith(FIELD_STRING, "test data 15").findAll();
        assertEquals(1, resultList.size());
    }

    // Test query 'or' with String field
    public void testRealmQueryOr() {
        doTestRealmQueryOr(AllTypes.class);
    }

    // Test query 'or' with indexed String field
    public void testRealmQueryOrWithIndex() {
        doTestRealmQueryOr(AllTypesIndex.class);
    }

    private void doTestRealmQueryOr(Class clazz) {
        populateTestRealm(200, clazz);

        RealmQuery query = testRealm.where(clazz).equalTo(FIELD_FLOAT, 31.234567f);
        RealmResults resultList = query.or().between(FIELD_LONG, 1, 20).findAll();
        assertEquals(21, resultList.size());

        resultList = query.or().equalTo(FIELD_STRING, "test data 15").findAll();
        assertEquals(21, resultList.size());

        resultList = query.or().equalTo(FIELD_STRING, "test data 117").findAll();
        assertEquals(22, resultList.size());
    }

    // Test query 'not' with long field
    public void testRealmQueryNot() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        doTestRealmQueryNot(AllTypes.class);
    }

    // Test query 'not' with indexed long field
    public void testRealmQueryNotWithIndex() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        doTestRealmQueryNot(AllTypesIndex.class);
    }

    private void doTestRealmQueryNot(Class clazz) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        populateTestRealm(clazz); // create TEST_DATA_SIZE objects

        // only one object with value 5 -> TEST_DATA_SIZE-1 object with value "not 5"
        RealmResults list1 = testRealm.where(clazz).not().equalTo(FIELD_LONG, 5).findAll();
        assertEquals(TEST_DATA_SIZE - 1, list1.size());

        // not().greater() and lessThenOrEqual() must be the same
        RealmResults list2 = testRealm.where(clazz).not().greaterThan(FIELD_LONG, 5).findAll();
        RealmResults list3 = testRealm.where(clazz).lessThanOrEqualTo(FIELD_LONG, 5).findAll();
        assertEquals(list2.size(), list3.size());
        for (int i = 0; i < list2.size(); i++) {
            RealmObject obj1 = list2.get(i);
            RealmObject obj2 = list3.get(i);
            long columnLong1 = (Long) obj1.getClass().getMethod("getColumnLong").invoke(obj1);
            long columnLong2 = (Long) obj2.getClass().getMethod("getColumnLong").invoke(obj2);
            assertEquals(columnLong1, columnLong2);
        }

        // excepted result: 0, 1, 2, 5
        long expected[] = {0, 1, 2, 5};
        RealmResults list4 = testRealm.where(clazz)
                .equalTo(FIELD_LONG, 5)
                .or()
                .not().beginGroup()
                    .greaterThan(FIELD_LONG, 2)
                 .endGroup()
                .findAll();
        assertEquals(4, list4.size());
        for (int i = 0; i < list4.size(); i++) {
            RealmObject obj1 = list4.get(i);
            long columnLong1 = (Long) obj1.getClass().getMethod("getColumnLong").invoke(obj1);
            assertEquals(expected[i], columnLong1);
        }
    }

    // Test query 'not' alone, must fail with UnsupportedOperationException
    public void testRealmQueryNotFailure() {
        doTestRealmQueryNotFailure(AllTypes.class);
    }

    // Test query 'not' alone, must fail with UnsupportedOperationException, with index version
    public void testRealmQueryNotFailureWithIndex() {
        doTestRealmQueryNotFailure(AllTypesIndex.class);
    }

    private void doTestRealmQueryNotFailure(Class clazz) {
        // a not() alone must fail
        try {
            testRealm.where(clazz).not().findAll();
            fail();
        } catch (UnsupportedOperationException ignored) {
        }
    }

    // Test query 'equalTo' and an implicit 'and' 'equalTo'
    public void testRealmQueryImplicitAnd() {
        doTestRealmQueryImplicitAnd(AllTypes.class);
    }

    // Test query 'equalTo' and an implicit 'and' 'equalTo', with index version
    public void testRealmQueryImplicitAndWithIndex() {
        doTestRealmQueryImplicitAnd(AllTypesIndex.class);
    }

    private void doTestRealmQueryImplicitAnd(Class clazz) {
        populateTestRealm(200, clazz);

        RealmQuery query = testRealm.where(clazz).equalTo(FIELD_FLOAT, 31.234567f);
        RealmResults resultList = query.between(FIELD_LONG, 1, 10).findAll();
        assertEquals(0, resultList.size());

        query = testRealm.where(clazz).equalTo(FIELD_FLOAT, 81.234567f);
        resultList = query.between(FIELD_LONG, 1, 100).findAll();
        assertEquals(1, resultList.size());
    }

    // Test query 'lessThan' on float field followed by query 'between' on long field
    public void testRealmQueryLessThan() {
        doTestRealmQueryLessThan(AllTypes.class);
    }

    // Test query 'lessThan' on float field followed by query 'between' on indexed long field
    public void testRealmQueryLessThanWithIndex() {
        doTestRealmQueryLessThan(AllTypesIndex.class);
    }

    private void doTestRealmQueryLessThan(Class clazz) {
        populateTestRealm(200, clazz);

        RealmResults resultList = testRealm.where(clazz).
                lessThan(FIELD_FLOAT, 31.234567f).findAll();
        assertEquals(30, resultList.size());
        RealmQuery query = testRealm.where(clazz).lessThan(FIELD_FLOAT, 31.234567f);
        resultList = query.between(FIELD_LONG, 1, 10).findAll();
        assertEquals(10, resultList.size());
    }

    public void testRealmQueryLessThanOrEqual() {
        doTestRealmQueryLessThanOrEqual(AllTypes.class);
    }

    public void testRealmQueryLessThanOrEqualWithIndex() {
        doTestRealmQueryLessThanOrEqual(AllTypesIndex.class);
    }

    private void doTestRealmQueryLessThanOrEqual(Class clazz) {
        populateTestRealm(200, clazz);

        RealmResults resultList = testRealm.where(clazz)
                .lessThanOrEqualTo(FIELD_FLOAT, 31.234567f).findAll();
        assertEquals(31, resultList.size());
        resultList = testRealm.where(clazz).lessThanOrEqualTo(FIELD_FLOAT, 31.234567f)
                .between(FIELD_LONG, 11, 20).findAll();
        assertEquals(10, resultList.size());
    }

    public void testRealmQueryEqualTo() {
        testRealmQueryEqualTo(AllTypes.class);
    }

    public void testRealmQueryEqualToWithIndex() {
        testRealmQueryEqualTo(AllTypesIndex.class);
    }

    private void testRealmQueryEqualTo(Class clazz) {
        populateTestRealm(200, clazz);

        RealmResults resultList = testRealm.where(clazz)
                .equalTo(FIELD_FLOAT, 31.234567f).findAll();
        assertEquals(1, resultList.size());
        resultList = testRealm.where(clazz).greaterThan(FIELD_FLOAT, 11.0f)
                .equalTo(FIELD_LONG, 10).findAll();
        assertEquals(1, resultList.size());
        resultList = testRealm.where(clazz).greaterThan(FIELD_FLOAT, 11.0f)
                .equalTo(FIELD_LONG, 1).findAll();
        assertEquals(0, resultList.size());
    }

    public void testRealmQueryEqualToNonLatinCharacters() {
        populateNonLatinTestRealm(200);

        RealmResults<NonLatinFieldNames> resultList = testRealm.where(NonLatinFieldNames.class)
                .equalTo(FIELD_LONG_KOREAN_CHAR, 13).findAll();
        assertEquals(1, resultList.size());
        resultList = testRealm.where(NonLatinFieldNames.class)
                .greaterThan(FIELD_FLOAT_KOREAN_CHAR, 11.0f)
                .equalTo(FIELD_LONG_KOREAN_CHAR, 10).findAll();
        assertEquals(1, resultList.size());
        resultList = testRealm.where(NonLatinFieldNames.class)
                .greaterThan(FIELD_FLOAT_KOREAN_CHAR, 11.0f)
                .equalTo(FIELD_LONG_KOREAN_CHAR, 1).findAll();
        assertEquals(0, resultList.size());

        resultList = testRealm.where(NonLatinFieldNames.class)
                .equalTo(FIELD_LONG_GREEK_CHAR, 13).findAll();
        assertEquals(1, resultList.size());
        resultList = testRealm.where(NonLatinFieldNames.class)
                .greaterThan(FIELD_FLOAT_GREEK_CHAR, 11.0f)
                .equalTo(FIELD_LONG_GREEK_CHAR, 10).findAll();
        assertEquals(1, resultList.size());
        resultList = testRealm.where(NonLatinFieldNames.class)
                .greaterThan(FIELD_FLOAT_GREEK_CHAR, 11.0f)
                .equalTo(FIELD_LONG_GREEK_CHAR, 1).findAll();
        assertEquals(0, resultList.size());
    }

    // Test query 'notEqualTo' float and long fields
    public void testRealmQueryNotEqualTo() {
        doTestRealmQueryNotEqualTo(AllTypes.class);
    }

    // Test query 'notEqualTo' float and indexed long fields
    public void testRealmQueryNotEqualToWithIndex() {
        doTestRealmQueryNotEqualTo(AllTypesIndex.class);
    }

    public void doTestRealmQueryNotEqualTo(Class clazz) {
        final int TEST_OBJECTS_COUNT = 200;
        populateTestRealm(TEST_OBJECTS_COUNT, clazz);

        RealmResults resultList = testRealm.where(clazz)
                .notEqualTo(FIELD_LONG, 31).findAll();
        assertEquals(TEST_OBJECTS_COUNT - 1, resultList.size());

        resultList = testRealm.where(clazz).notEqualTo(FIELD_FLOAT, 11.234567f)
                .equalTo(FIELD_LONG, 10).findAll();
        assertEquals(0, resultList.size());

        resultList = testRealm.where(clazz).notEqualTo(FIELD_FLOAT, 11.234567f)
                .equalTo(FIELD_LONG, 1).findAll();
        assertEquals(1, resultList.size());
    }

    // Test query 'contains' with different case sensitive on a String field.
    public void testRealmQueryContainsAndCaseSensitive() {
        doTestRealmQueryContainsAndCaseSensitive(AllTypes.class);
    }

    // Test query 'contains' with different case sensitive on a indexed String field.
    public void testRealmQueryContainsAndCaseSensitiveWithIndex() {
        doTestRealmQueryContainsAndCaseSensitive(AllTypesIndex.class);
    }

    private void doTestRealmQueryContainsAndCaseSensitive(Class clazz) {
        final int TEST_OBJECTS_COUNT = 200;
        populateTestRealm(TEST_OBJECTS_COUNT, clazz);

        RealmResults resultList = testRealm.where(clazz)
                .contains("columnString", "DaTa 0", RealmQuery.CASE_INSENSITIVE)
                .or().contains("columnString", "20")
                .findAll();
        assertEquals(3, resultList.size());

        resultList = testRealm.where(clazz).contains("columnString", "DATA").findAll();
        assertEquals(0, resultList.size());

        resultList = testRealm.where(clazz)
                .contains("columnString", "TEST", RealmQuery.CASE_INSENSITIVE).findAll();
        assertEquals(TEST_OBJECTS_COUNT, resultList.size());
    }

    // Test query 'contains' with different case sensitive on a String field with non-latin chars
    public void testRealmQueryContainsAndCaseSensitiveWithNonLatinCharacters()
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        doTestRealmQueryContainsAndCaseSensitiveWithNonLatinCharacters(AllTypes.class);
    }

    // Test query 'contains' with different case sensitive on a indexed String field with non-latin chars
    public void testRealmQueryContainsAndCaseSensitiveWithNonLatinCharactersWithIndex()
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        doTestRealmQueryContainsAndCaseSensitiveWithNonLatinCharacters(AllTypesIndex.class);
    }

    private void doTestRealmQueryContainsAndCaseSensitiveWithNonLatinCharacters(Class clazz)
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        populateTestRealm(clazz);

        testRealm.beginTransaction();
        testRealm.clear(clazz);
        RealmObject at1 = testRealm.createObject(clazz);
        clazz.getMethod("setColumnString", String.class).invoke(at1, "Αλφα");
        RealmObject at2 = testRealm.createObject(clazz);
        clazz.getMethod("setColumnString", String.class).invoke(at2, "βήτα");
        RealmObject at3 = testRealm.createObject(clazz);
        clazz.getMethod("setColumnString", String.class).invoke(at3, "δέλτα");
        testRealm.commitTransaction();

        RealmResults resultList = testRealm.where(clazz)
                .contains("columnString", "Α", RealmQuery.CASE_INSENSITIVE)
                .or().contains("columnString", "δ")
                .findAll();
        // Without case sensitive there is 3, Α = α
        // assertEquals(3,resultList.size());
        assertEquals(2, resultList.size());

        resultList = testRealm.where(clazz).contains("columnString", "α").findAll();
        assertEquals(3, resultList.size());

        resultList = testRealm.where(clazz).contains("columnString", "Δ").findAll();
        assertEquals(0, resultList.size());

        resultList = testRealm.where(clazz).contains("columnString", "Δ",
                RealmQuery.CASE_INSENSITIVE).findAll();
        // Without case sensitive there is 1, Δ = δ
        // assertEquals(1,resultList.size());
        assertEquals(0, resultList.size());
    }

    public void testQueryWithNonExistingField() {
        try {
            testRealm.where(AllTypes.class).equalTo("NotAField", 13).findAll();
            fail("Should throw exception");
        } catch (IllegalArgumentException ignored) {
        }
    }

    public void testRealmQueryLink() {
        testRealm.beginTransaction();
        Owner owner = testRealm.createObject(Owner.class);
        Dog dog1 = testRealm.createObject(Dog.class);
        dog1.setName("Dog 1");
        dog1.setWeight(1);
        Dog dog2 = testRealm.createObject(Dog.class);
        dog2.setName("Dog 2");
        dog2.setWeight(2);
        owner.getDogs().add(dog1);
        owner.getDogs().add(dog2);
        testRealm.commitTransaction();

        // Dog.weight has index 4 which is more than the total number of columns in Owner
        // This tests exposes a subtle error where the Owner tablespec is used instead of Dog tablespec.
        RealmResults<Dog> dogs = testRealm.where(Owner.class).findFirst().getDogs().where()
                .findAllSorted("name", RealmResults.SORT_ORDER_ASCENDING);
        Dog dog = dogs.where().equalTo("weight", 1d).findFirst();
        assertEquals(dog1, dog);
    }


    public void testSortMultiFailures() {
        // zero fields specified
        try {
            testRealm.where(AllTypes.class)
                    .findAllSorted(new String[]{}, new boolean[]{});
            fail();
        } catch (IllegalArgumentException ignored) {}

        // number of fields and sorting orders don't match
        try {
            testRealm.where(AllTypes.class)
                    .findAllSorted(new String[]{FIELD_STRING},
                            new boolean[]{RealmResults.SORT_ORDER_ASCENDING, RealmResults.SORT_ORDER_ASCENDING});
            fail();
        } catch (IllegalArgumentException ignored) {}

        // null is not allowed
        try {
            testRealm.where(AllTypes.class).findAllSorted(null, null);
            fail();
        } catch (IllegalArgumentException ignored) {}
        try {
            testRealm.where(AllTypes.class).findAllSorted(new String[]{FIELD_STRING},
                    null);
            fail();
        } catch (IllegalArgumentException ignored) {}

        // non-existing field name
        try {
            testRealm.where(AllTypes.class)
                    .findAllSorted(new String[]{FIELD_STRING, "dont-exist"},
                            new boolean[]{RealmResults.SORT_ORDER_ASCENDING, RealmResults.SORT_ORDER_ASCENDING});
            fail();
        } catch (IllegalArgumentException ignored) {}
    }

    public void testSortSingleField() {
        testRealm.beginTransaction();
        for (int i = 0; i < TEST_DATA_SIZE; i++) {
            AllTypes allTypes = testRealm.createObject(AllTypes.class);
            allTypes.setColumnLong(i);
        }
        testRealm.commitTransaction();

        RealmResults<AllTypes> sortedList = testRealm.where(AllTypes.class)
                .findAllSorted(new String[]{FIELD_LONG}, new boolean[]{RealmResults.SORT_ORDER_DESCENDING});
        assertEquals(TEST_DATA_SIZE, sortedList.size());
        assertEquals(TEST_DATA_SIZE - 1, sortedList.first().getColumnLong());
        assertEquals(0, sortedList.last().getColumnLong());
    }

    public void testSubQueryScope() {
        populateTestRealm(AllTypes.class);
        RealmResults<AllTypes> result = testRealm.where(AllTypes.class).lessThan("columnLong", 5).findAll();
        RealmResults<AllTypes> subQueryResult = result.where().greaterThan("columnLong", 3).findAll();
        assertEquals(1, subQueryResult.size());
    }

    public void testFindFirst() {
        testRealm.beginTransaction();
        Owner owner1 = testRealm.createObject(Owner.class);
        owner1.setName("Owner 1");
        Dog dog1 = testRealm.createObject(Dog.class);
        dog1.setName("Dog 1");
        dog1.setWeight(1);
        Dog dog2 = testRealm.createObject(Dog.class);
        dog2.setName("Dog 2");
        dog2.setWeight(2);
        owner1.getDogs().add(dog1);
        owner1.getDogs().add(dog2);

        Owner owner2 = testRealm.createObject(Owner.class);
        owner2.setName("Owner 2");
        Dog dog3 = testRealm.createObject(Dog.class);
        dog3.setName("Dog 3");
        dog3.setWeight(1);
        Dog dog4 = testRealm.createObject(Dog.class);
        dog4.setName("Dog 4");
        dog4.setWeight(2);
        owner2.getDogs().add(dog3);
        owner2.getDogs().add(dog4);
        testRealm.commitTransaction();

        RealmList<Dog> dogs = testRealm.where(Owner.class).equalTo("name", "Owner 2").findFirst().getDogs();
        Dog dog = dogs.where().equalTo("name", "Dog 4").findFirst();
        assertEquals(dog4, dog);
    }

    public void testGeorgian() {
        String words[] = {"მონაცემთა ბაზა", "მიწისქვეშა გადასასვლელი", "რუსთაველის გამზირი",
                "მთავარი ქუჩა", "სადგურის მოედანი", "ველოცირაპტორების ჯოგი"};
        String sorted[] = {"ველოცირაპტორების ჯოგი", "მთავარი ქუჩა", "მიწისქვეშა გადასასვლელი",
                "მონაცემთა ბაზა", "რუსთაველის გამზირი", "სადგურის მოედანი"};

        testRealm.beginTransaction();
        testRealm.clear(StringOnly.class);
        for (String word : words) {
            StringOnly stringOnly = testRealm.createObject(StringOnly.class);
            stringOnly.setChars(word);
        }
        testRealm.commitTransaction();

        RealmResults<StringOnly> stringOnlies1 = testRealm.where(StringOnly.class).contains("chars", "მთავარი").findAll();
        assertEquals(1, stringOnlies1.size());

        RealmResults<StringOnly> stringOnlies2 = testRealm.allObjects(StringOnly.class);
        stringOnlies2.sort("chars");
        for (int i = 0; i < stringOnlies2.size(); i++) {
            assertEquals(sorted[i], stringOnlies2.get(i).getChars());
        }
    }
}
