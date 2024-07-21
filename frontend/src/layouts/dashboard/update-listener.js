import {
    UserInformationSocketListener
} from '../../helpers/helper-rest-functions';
import {withAuthGuard} from '../../hocs/with-auth-guard';

export const UpdateListener = withAuthGuard((props) => {
    const {children} = props;
    const userId = window.sessionStorage.getItem('userId')
    UserInformationSocketListener(userId);
    return <>
        {children}
    </>;
});