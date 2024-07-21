import { Layout as DashboardLayout } from '../layouts/dashboard/layout';
import { Box, Container, Typography, Unstable_Grid2 as Grid } from '@mui/material';
import Head from 'next/head';

const Page = () => {
  return (
    <>
      <Head>
        <title>
          Games | eCrypt
        </title>
      </Head>
      <Box
        component="main"
        sx={{
          flexGrow: 1,
          py: 8
        }}
      >
        <Container maxWidth="xl">
          <Grid
            container
            spacing={3}
          >
            <Typography variant="h6">
              Need help ? Write to nimofy1997@gmail.com and one of our support team member will respond to you as soon as he can
            </Typography>
          </Grid>
        </Container>
      </Box>
    </>
  );
};

Page.getLayout = (page) => (
  <DashboardLayout>
    {page}
  </DashboardLayout>
);

export default Page;