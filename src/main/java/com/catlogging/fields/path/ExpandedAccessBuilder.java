package com.catlogging.fields.path;

import com.catlogging.fields.FieldsMap;

public interface ExpandedAccessBuilder {
	<T> ExpandedAccess<T> buildAccess(FieldsMap fields, String path, Class<T> desiredClass);
}
