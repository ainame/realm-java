/*
 * Copyright 2015 Realm Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.realm.dynamic;

import java.util.Date;

import io.realm.Realm;
import io.realm.internal.CheckedRow;
import io.realm.internal.Row;
import io.realm.internal.UncheckedRow;

/**
 * Class that wraps a normal RealmObject which enables interaction using dynamic names.
 */
public class DynamicRealmObject {

     Realm realm;
     Row row;

    /**
     * Creates a dynamic Realm object based on a row entry.
     */
    public DynamicRealmObject(Realm realm, Row row) {
        this.realm = realm;
        this.row = (row instanceof CheckedRow) ? (CheckedRow) row : ((UncheckedRow) row).convertToChecked();
    }

    /**
     * Returns the objects {@code boolean} value for a given field.
     *
     * @param fieldName Name of field.
     * @return The boolean value.
     * @throws IllegalArgumentException if field name doesn't exists or it doesn't contain booleans.
     */
    public boolean getBoolean(String fieldName) {
        long columnIndex = row.getColumnIndex(fieldName);
        return row.getBoolean(columnIndex);
    }

    /**
     * Returns the objects {@code int} value for a given field.
     *
     * @param fieldName Name of field.
     * @return The int value. Integer values exceeding {@code Integer.MAX_VALUE} will wrap.
     * @throws IllegalArgumentException if field name doesn't exists or it doesn't contain integers.
     */
    public int getInt(String fieldName) {
        return (int) getLong(fieldName);
    }

    /**
     * Returns the objects {@code short} value for a given field.
     *
     * @param fieldName Name of field.
     * @return The short value. Integer values exceeding {@code Short.MAX_VALUE} will wrap.
     * @throws IllegalArgumentException if field name doesn't exists or it doesn't contain integers.
     */
    public short getShort(String fieldName) {
        return (short) getLong(fieldName);
    }

    /**
     * Returns the objects {@code long} value for a given field.
     *
     * @param fieldName Name of field.
     * @return The long value. Integer values exceeding {@code Long.MAX_VALUE} will wrap.
     * @throws IllegalArgumentException if field name doesn't exists or it doesn't contain integers.
     */
    public long getLong(String fieldName) {
        long columnIndex = row.getColumnIndex(fieldName);
        return row.getLong(columnIndex);
    }

    /**
     * Returns the objects {@code float} value for a given field.
     *
     * @param fieldName Name of field.
     * @return The float value.
     * @throws IllegalArgumentException if field name doesn't exists or it doesn't contain floats.
     */
    public float getFloat(String fieldName) {
        long columnIndex = row.getColumnIndex(fieldName);
        return row.getFloat(columnIndex);
    }

    /**
     * Returns the objects {@code double} value for a given field.
     *
     * @param fieldName Name of field.
     * @return The double value.
     * @throws IllegalArgumentException if field name doesn't exists or it doesn't contain doubles.
     */
    public double getDouble(String fieldName) {
        long columnIndex = row.getColumnIndex(fieldName);
        return row.getDouble(columnIndex);
    }

    /**
     * Returns the objects {@code byte[]} value for a given field.
     *
     * @param fieldName Name of field.
     * @return The byte[] value.
     * @throws IllegalArgumentException if field name doesn't exists or it doesn't contain binary data.
     */
    public byte[] getBytes(String fieldName) {
        long columnIndex = row.getColumnIndex(fieldName);
        return row.getBinaryByteArray(columnIndex);
    }

    /**
     * Returns the objects {@code String} value for a given field.
     *
     * @param fieldName Name of field.
     * @return The String value.
     * @throws IllegalArgumentException if field name doesn't exists or it doesn't contain Strings.
     */
    public String getString(String fieldName) {
        long columnIndex = row.getColumnIndex(fieldName);
        return row.getString(columnIndex);
    }

    /**
     * Returns the objects {@code Date} value for a given field.
     *
     * @param fieldName Name of field.
     * @return The Date value.
     * @throws IllegalArgumentException if field name doesn't exists or it doesn't contain Dates.
     */
    public Date getDate(String fieldName) {
        long columnIndex = row.getColumnIndex(fieldName);
        return row.getDate(columnIndex);
    }

    /**
     * Returns the object being linked to from this field..
     *
     * @param fieldName Name of field.
     * @return The {@link DynamicRealmObject} representation of the linked object or {@code null} if no object is linked.
     * @throws IllegalArgumentException if field name doesn't exists or it doesn't contain links to other objects.
     */
    public DynamicRealmObject getRealmObject(String fieldName) {
        long columnIndex = row.getColumnIndex(fieldName);
        long linkRowIndex = row.getLink(columnIndex);
        CheckedRow linkRow = row.getTable().getCheckedRow(linkRowIndex);
        return new DynamicRealmObject(realm, linkRow);
    }

    /**
     * Returns the {@link io.realm.RealmList} of objects being linked to from this field.
     *
     * @param fieldName Name of field.
     * @return The {@link DynamicRealmList} representation of the RealmList.
     * @throws IllegalArgumentException if field name doesn't exists or it doesn't contain a list of links.
     */
    public DynamicRealmList getRealmList(String fieldName) {
        long columnIndex = row.getColumnIndex(fieldName);
        return new DynamicRealmList(row.getLinkList(columnIndex), realm);
    }

    /**
     * Checks if the value of a given is {@code null}.
     *
     * @param fieldName Name of field.
     * @return {@code true} if field value is null, {@code false} otherwise.
     * @throws IllegalArgumentException if field name doesn't exists.
     */
    public boolean isNull(String fieldName) {
        long columnIndex = row.getColumnIndex(fieldName);
        return row.isNullLink(columnIndex); // TODO Add support for other types
    }

    /**
     * Checks whether an object has the given field or not.
     * @param fieldName Field name to check.
     * @return {@code true} if the object has a field with the given name, {@code false} otherwise.
     */
    public boolean hasField(String fieldName) {
        if (fieldName == null || fieldName.isEmpty()) {
            return false;
        }
        return row.hasField(fieldName);
    }

    /**
     * Returns the list of field names on this object.
     *
     * @return List of field names on this objects or the empty list if the object doesn't have any fields.
     */
    public String[] getFieldNames() {
        String[] keys = new String[(int) row.getColumnCount()];
        for (int i = 0; i < keys.length; i++) {
            keys[i] = row.getColumnName(i);
        }
        return keys;
    }

    /**
     * Sets the boolean value of the given field on the object.
     *
     * @param fieldName Field name to update.
     * @param value Value to insert.
     * @throws IllegalArgumentException if field name doesn't exists or isn't a boolean field.
     */
    public void setBoolean(String fieldName, boolean value) {

    }

    /**
     * Sets the short value of the given field on the object.
     *
     * @param fieldName Field name.
     * @param value Value to insert.
     * @throws IllegalArgumentException if field name doesn't exists or isn't an integer field.
     */
    public void setShort(String fieldName, short value) {

    }

    /**
     * Sets the integer value of the given field on the object.
     *
     * @param fieldName Field name to update.
     * @param value Value to insert.
     * @throws IllegalArgumentException if field name doesn't exists or isn't an integer field.
     */
    public void setInt(String fieldName, int value) {

    }

    /**
     * Sets the long value of the given field on the object.
     *
     * @param fieldName Field name.
     * @param value Value to insert.
     * @throws IllegalArgumentException if field name doesn't exists or isn't an integer field.
     */
    public void setLong(String fieldName, long value) {

    }

    /**
     * Sets the float value of the given field on the object.
     *
     * @param fieldName Field name.
     * @param value Value to insert.
     * @throws IllegalArgumentException if field name doesn't exists or isn't an integer field.
     */
    public void setFloat(String fieldName, float value) {

    }

    /**
     * Sets the double value of the given field on the object.
     *
     * @param fieldName Field name.
     * @param value Value to insert.
     * @throws IllegalArgumentException if field name doesn't exists or isn't a double field.
     */
    public void setDouble(String fieldName, double value) {

    }

    /**
     * Sets the String value of the given field on the object.
     *
     * @param fieldName Field name.
     * @param value Value to insert.
     * @throws IllegalArgumentException if field name doesn't exists or isn't a String field.
     */
    public void setString(String fieldName, String value) {

    }

    /**
     * Sets the binary value of the given field on the object.
     *
     * @param fieldName Field name.
     * @param value Value to insert.
     * @throws IllegalArgumentException if field name doesn't exists or isn't a binary field.
     */
    public void setBinary(String fieldName, byte[] value) {

    }

    /**
     * Sets the date value of the given field on the object.
     *
     * @param fieldName Field name.
     * @param value Value to insert.
     * @throws IllegalArgumentException if field name doesn't exists or isn't a Date field.
     */
    public void setDate(String fieldName, Date value) {

    }

    /**
     * Sets the reference to another object on the given field.
     *
     * @param fieldName Field name.
     * @param value Object to link to.
     * @throws IllegalArgumentException if field name doesn't exists, it doesn't link to other Realm objects, or the type
     * of DynamicRealmObject doesn't match.
     */
    public void setObject(String fieldName, DynamicRealmObject value) {

    }

    /**
     * Sets the RealmList on the object.
     *
     * @param fieldName Field name.
     * @param value List of references.
     * @throws IllegalArgumentException if field name doesn't exists, it doesn't contain a list of links or the type
     * of the object represented by the DynamicRealmObject doesn't match.
     */
    public void setList(String fieldName, DynamicRealmList value) {

    }

    @Override
    public int hashCode() {
        String realmName = realm.getPath();
        String tableName = row.getTable().getName();
        long rowIndex = row.getIndex();

        int result = 17;
        result = 31 * result + ((realmName != null) ? realmName.hashCode() : 0);
        result = 31 * result + ((tableName != null) ? tableName.hashCode() : 0);
        result = 31 * result + (int) (rowIndex ^ (rowIndex >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DynamicRealmObject other = (DynamicRealmObject) o;

        String path = realm.getPath();
        String otherPath = other.realm.getPath();
        if (path != null ? !path.equals(otherPath) : otherPath != null) {
            return false;
        }

        String tableName = row.getTable().getName();
        String otherTableName = other.row.getTable().getName();
        if (tableName != null ? !tableName.equals(otherTableName) : otherTableName != null) {
            return false;
        }

        if (row.getIndex() != other.row.getIndex()) {
            return false;
        }

        return true;
    }

    @Override
    public String toString() {
        return super.toString(); // TODO How to iterate across all fields?
    }
    
}