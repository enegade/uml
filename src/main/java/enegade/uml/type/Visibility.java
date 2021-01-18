package enegade.uml.type;

/*
 * Copyright 2020 enegade
 *
 * This file is part of the enegade.uml package.
 *
 * This file is distributed "AS IS", WITHOUT ANY WARRANTIES.
 *
 * For the full copyright and license information, please view the LICENSE
 * file distributed with this project.
 */

import com.sun.jdi.Accessible;
import enegade.uml.excepion.LogicException;

/**
 * @author enegade
 */
public enum Visibility
{
    PUBLIC, PRIVATE, PACKAGE_PRIVATE, PROTECTED;

    public static Visibility getVisibility(Accessible accessible)
    {
        if( accessible.isPublic() ) {
            return Visibility.PUBLIC;
        } else if ( accessible.isPrivate() ) {
            return Visibility.PRIVATE;
        } else if ( accessible.isPackagePrivate() ) {
            return Visibility.PACKAGE_PRIVATE;
        } else if ( accessible.isProtected() ) {
            return Visibility.PROTECTED;
        } else {
            throw new LogicException();
        }
    }
}
