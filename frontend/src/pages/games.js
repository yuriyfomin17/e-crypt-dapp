import Head from 'next/head';
import {
  Box,
  Container, Typography,
  Unstable_Grid2 as Grid
} from '@mui/material';
import { Layout as DashboardLayout } from 'src/layouts/dashboard/layout';
import { UserBetCard } from '../sections/user/user-bet-card';
import { AdminBetCard } from '../sections/admin/admin-bet-card';
import { ROLES } from '../helpers/helper-constants';
import { getBetsFromCache, getUserRole } from '../helpers/helper-functions';
import { useEffect, useState } from 'react';

const Page = () => {
  const role = getUserRole()
  const [bets, setBets] = useState(getBetsFromCache())

  const action = role === ROLES.ROLE_ADMIN ? 'CREATE AND GIVE ID TO FOLLOWERS' : 'ADD BY ID FROM ADMIN';
  useEffect(() => {
    const timer = setInterval(() => {
      setBets(getBetsFromCache())
    }, 1000);
    return () => {
      clearInterval(timer);
    };
  }, []);
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
            {bets && bets.length === 0 &&
              <Typography variant="h6">
                NO GAMES. {action}
              </Typography>
            }

            {bets.map((currentBet, idx) => (<Grid
              xs={12}
              md={12}
              lg={12}
              key={idx}
            >
              {role === ROLES.ROLE_USER &&
                <UserBetCard currentBet={currentBet}/>
              }
              {role === ROLES.ROLE_ADMIN &&
                <AdminBetCard currentBet={currentBet}/>
              }
            </Grid>))}
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
