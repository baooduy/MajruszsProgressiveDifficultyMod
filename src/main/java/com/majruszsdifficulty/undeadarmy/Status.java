package com.majruszsdifficulty.undeadarmy;

public enum Status {
	BETWEEN_WAVES, ONGOING, VICTORY, FAILED, STOPPED;

	public static Status getByName( String name ) {
		for( Status status : Status.values() )
			if( name.equalsIgnoreCase( status.name() ) )
				return status;

		return ONGOING;
	}
}
