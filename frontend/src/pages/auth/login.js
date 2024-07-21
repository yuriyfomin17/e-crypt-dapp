import Head from 'next/head';

import {
    Box,
} from '@mui/material';
import {useAuth} from '/src/hooks/use-auth';
import {Layout as AuthLayout} from '/src/layouts/auth/layout';
import {GoogleLogin} from '@react-oauth/google';
import {useEffect} from "react";
import {useRouter} from "next/router";

const Page = () => {
    const auth = useAuth();
    const router = useRouter();
    useEffect( () => {
        const token = window.sessionStorage.getItem('token')
        if (token){
             router.push('/');
        }
    }, []);
    return (
        <>
            <Head>
                <title>
                    Login | eCrypt
                </title>
            </Head>
            <Box
                sx={{
                    backgroundColor: 'background.paper',
                    flex: '1 1 auto',
                    alignItems: 'center',
                    display: 'flex',
                    justifyContent: 'center'
                }}
            >
                <GoogleLogin
                    size="large"
                    theme="filled_blue"
                    shape="circle"
                    onSuccess={async (credentialResponse) => {
                        await auth.signInOrRegister(credentialResponse.credential);
                    }} onError={() => {
                    console.log('failed to login');
                }}/>
            </Box>
        </>

    );
};

Page.getLayout = (page) => (
    <AuthLayout>
        {page}
    </AuthLayout>
);

export default Page;
